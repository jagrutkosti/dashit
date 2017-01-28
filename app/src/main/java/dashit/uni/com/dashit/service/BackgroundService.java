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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

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
    static String accidentLocation;

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

        // Create new SurfaceView, set its size to 50x50, move it to the top right corner and set this service as a callback
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        surfaceView = new SurfaceView(this);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, displaymetrics.heightPixels, getResources().getDisplayMetrics());
        width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, displaymetrics.widthPixels, getResources().getDisplayMetrics());

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                (int) (width * 0.02), (int) (height * 0.012),
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        windowManager.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);
    }

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
                                Thread.sleep(20000);
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
                        Thread.sleep(20000);
                        stopRecording();
                        orderAndSaveVideos();
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
            e.printStackTrace();
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
     * When received a confirmation of collision, order the videos in correct order and save them.
     * Initiate PostCollisionTasksService to handle further tasks.
     */
    public void orderAndSaveVideos() {
        int[] orderOfVideo = new int[3];

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

                    fileIS.close();
                    target.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //Initate PostCollisionTasksService to handle other tasks.
        Intent postCollisionTasks = new Intent(getApplicationContext(), PostCollisionTasksService.class);
        postCollisionTasks.putExtra("directoryPath", dir.getAbsolutePath());
        postCollisionTasks.putExtra("accidentLocation", accidentLocation);
        startService(postCollisionTasks);
        stopSelf();
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
            accidentLocation = (String) intent.getExtras().get("accidentLocation");
            accidentStatus = true;
        }
    }
}
