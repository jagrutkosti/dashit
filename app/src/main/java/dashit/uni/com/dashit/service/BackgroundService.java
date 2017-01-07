/**
 * Created by Jagrut on 23-Jan-16.
 */

package dashit.uni.com.dashit.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import dashit.uni.com.dashit.ActivityLifeCycleHandler;
import dashit.uni.com.dashit.DashItApplication;
import dashit.uni.com.dashit.R;
import dashit.uni.com.dashit.view.activity.MainActivity;

/**
 * The main application logic. All background tasks are synchronized here.
 * That includes, Video Recording, File Saving, Hash Creation, Hash Transmission
 */
public class BackgroundService extends Service implements SurfaceHolder.Callback {

    private boolean recordingStatus;
    static boolean accidentStatus = false;
    boolean manualStopStatus = false;
    int accidentOnVideoIndex = 0;
    Handler handler;
    Handler screenSizeHandler;

    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;
    SurfaceHolder globalHolder;
    Thread thread = null;

    int height = 0;
    int width = 0;


    /**
     * Create a surface to hold the camera preview. This surface rests above all surface.
     * Create Notification to let the user know anytime that the application is running.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();
        screenSizeHandler = new Handler();

        if (isExternalStorageWritable()) {
            for (int i = 1; i < 4; i++) {
                File delPreviousFiles = new File(Environment.getExternalStorageDirectory().toString() + "/dashit" + i + ".mp4");
                delPreviousFiles.delete();
            }
        } else {
            Toast.makeText(BackgroundService.this, "Storage device not available.", Toast.LENGTH_LONG).show();
            onDestroy();
        }

        //Intent to start when user taps on notification
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, 0);

        // Start foreground service to avoid unexpected kill
        Notification notification = new Notification.Builder(this)
                .setContentTitle("DashIt")
                .setContentText("Video is recorded in background.")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(contentIntent)
                .build();

        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
        startForeground(Integer.MAX_VALUE, notification);

        // Create new SurfaceView, set its size to 50x50, move it to the top left corner and set this service as a callback
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        surfaceView = new SurfaceView(this);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
        width = displaymetrics.widthPixels;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                50, 50,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        windowManager.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);

        mStatusChecker.run();
    }


    /**
     * A thread which checks if any activity is in foreground or not.
     * Depending on that it updates the surface to tiny tile on right side of screen or big tile
     * in center.
     */
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (ActivityLifeCycleHandler.isApplicationVisible() || ActivityLifeCycleHandler.isApplicationInForeground()) {
                    // is in foreground
                   WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                            width, height - 500,
                            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSLUCENT
                    );
                    windowManager.updateViewLayout(surfaceView, layoutParams);

                } else {
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                            50, 50,
                            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSLUCENT
                    );
                    layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
                    windowManager.updateViewLayout(surfaceView, layoutParams);
                }
            } finally {

                screenSizeHandler.postDelayed(mStatusChecker, 500);
            }
        }
    };

    /**
     * When the surface is created, it will start recording the video in infinite loop.
     * The variable 'accidentStatus' changes in MyBroadcastReceiver when a collision is detected
     * to notify when to stop video recording
     *
     * @param holder The Surface holder on which to attach the camera preview
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        globalHolder = holder;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!accidentStatus) {
                    int i = 1;
                    while (i < 3) {
                        if (!accidentStatus && !manualStopStatus) {
                            accidentOnVideoIndex = i;
                            startRecording("dashit" + i);
                            try {
                                Thread.sleep(10000);
                                if (recordingStatus)
                                    stopRecording();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                }
                if (!manualStopStatus) {

                    startRecording("dashit3");
                    try {
                        Thread.sleep(10000);
                        stopRecording();
                        screenSizeHandler.removeCallbacks(mStatusChecker);
                        generateHash();
                        windowManager.removeView(surfaceView);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Cleanup tasks when the background service exits: either manually by user or due to collision
     */
    @Override
    public void onDestroy() {
        manualStopStatus = true;
        if (recordingStatus) {
            stopRecording();
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        windowManager.removeView(surfaceView);
        screenSizeHandler.removeCallbacks(mStatusChecker);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Check to find if the device is in proper state to handle File write
     *
     * @return {boolean} writable or not
     */
    public boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Start recording the video
     *
     * @param fileName store the following video recording under this name
     */
    public void startRecording(String fileName) {
        recordingStatus = true;
        camera = Camera.open();
        mediaRecorder = new MediaRecorder();
        camera.setDisplayOrientation(90);
        camera.unlock();

        mediaRecorder.setPreviewDisplay(globalHolder.getSurface());
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().toString() + "/" + fileName + ".mp4");

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
        }
        mediaRecorder.start();
    }

    /**
     * Stop recording video. Executed when called.
     */
    public void stopRecording() {
        recordingStatus = false;
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        camera.lock();
        camera.release();
    }

    /**
     * Executed only when a collision is detected.
     * Order the videos in correct order.
     * Get the video files in that order and move them to another location under a new directory.
     * Create a byte array of video files and generate hash.
     */
    public void generateHash() {
        int[] orderOfVideo = new int[3];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (accidentOnVideoIndex == 2) {
            orderOfVideo[0] = 1;
            orderOfVideo[1] = 2;
        } else {
            orderOfVideo[0] = 2;
            orderOfVideo[1] = 1;
        }
        orderOfVideo[2] = 3;

        File dir = new File(Environment.getExternalStorageDirectory().toString() + "/dashitHistory/" + DateFormat.format("dd-MM-yyyy HH:mm", new Date().getTime()));
        if (!dir.isDirectory())
            dir.mkdirs();

        for (int i = 0; i < 3; i++) {
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/dashit" + orderOfVideo[i] + ".mp4");
            if (file.exists() && !file.isDirectory()) {
                byte[] byteArray = new byte[(int) file.length()];
                try {
                    FileOutputStream target = new FileOutputStream(dir.getPath() + "/" + (i + 1) + "accVideo" + orderOfVideo[i] + ".mp4");
                    InputStream fileIS = new FileInputStream(file);
                    fileIS.read(byteArray);

                    target.write(byteArray);
                    outputStream.write(byteArray);
                    fileIS.close();
                    target.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        byte[] finalByte = outputStream.toByteArray();
        Log.i("Final Byte Array Length", "" + finalByte.length);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(finalByte);
            byte[] mdBytes = md.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < mdBytes.length; i++) {
                hexString.append(Integer.toHexString(0xFF & mdBytes[i]));
            }
            Log.i("Hex format : ", "" + hexString.toString());

            //Create a hash.txt file
            File hash = new File(dir.getPath() + "/hash.txt");
            FileWriter writer = new FileWriter(hash);
            writer.append(hexString.toString());
            writer.flush();
            writer.close();
            outputStream.close();
            sendHashToServer(hexString.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send the generated hash to Originstamp server
     *
     * @param hashString the hash to be submitted
     */
    public void sendHashToServer(String hashString) {
        String url = "http://www.originstamp.org/api/stamps";
        String postData = "{\"hash_sha256\" : \"" + hashString + "\"}";
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Token token=\"a876e0bbb8894e8c8eadc5b3a19adff7\"");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            con.setRequestProperty("Accept", "*/*");
            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            dos.writeBytes(postData);
            dos.flush();
            dos.close();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DashItApplication.getAppContext(), "Hash sent successfully!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BackgroundService.this, "Problems while sending hash. Please check you internet connection.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BackgroundService.this, "Problems while sending hash. Please check you internet connection.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * The class which listens to any collision event from SensorService
     */
    public static class CollisionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            accidentStatus = true;
        }
    }
}
