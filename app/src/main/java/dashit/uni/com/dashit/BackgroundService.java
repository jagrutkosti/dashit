/**
 * Created by Jagrut on 23-Jan-16.
 */

package dashit.uni.com.dashit;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service implements SurfaceHolder.Callback{

    private boolean recordingStatus;
    static boolean accidentStatus = false;
    boolean manualStopStatus = false;
    int accidentOnVideoIndex = 0;
    Handler handler;

    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;
    SurfaceHolder globalHolder;
    Thread thread = null;

    int height = 0;
    int width = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();

        if(isExternalStorageWritable()){
            for (int i = 1; i < 4; i++) {
                File delPreviousFiles = new File(Environment.getExternalStorageDirectory().toString()+"/dashit" + i + ".mp4");
                delPreviousFiles.delete();
            }
        }else{
            Toast.makeText(BackgroundService.this, "Storage device not available.", Toast.LENGTH_LONG).show();
            onDestroy();
        }

        // Start foreground service to avoid unexpected kill
        Notification notification = new Notification.Builder(this)
                .setContentTitle("DashIt")
                .setContentText("The app is running.")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        startForeground(1234, notification);

        // Create new SurfaceView, set its size to 50x50, move it to the top left corner and set this service as a callback
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        surfaceView = new SurfaceView(this);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
        width = displaymetrics.widthPixels;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                width,height - 500,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        //layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        windowManager.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);

        mStatusChecker.run();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if(MyLifeCycleHandler.isApplicationVisible() || MyLifeCycleHandler.isApplicationInForeground()){
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                            width,height - 500,
                            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSLUCENT
                    );
                    windowManager.updateViewLayout(surfaceView, layoutParams);
                }else{
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                            50,50,
                            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSLUCENT
                    );
                    layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
                    windowManager.updateViewLayout(surfaceView, layoutParams);
                }
            } finally {

                handler.postDelayed(mStatusChecker, 1000);
            }
        }
    };

    @Override
    public void onDestroy() {
        manualStopStatus = true;
        if(recordingStatus){
            stopRecording();
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        windowManager.removeView(surfaceView);
        handler.removeCallbacks(mStatusChecker);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    public void startRecording(String fileName){
        recordingStatus = true;
        camera = Camera.open();
        mediaRecorder = new MediaRecorder();
        camera.setDisplayOrientation(90);
        camera.unlock();

        mediaRecorder.setPreviewDisplay(globalHolder.getSurface());
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().toString() + "/" + fileName + ".mp4");

        try { mediaRecorder.prepare(); } catch (Exception e) {}
        mediaRecorder.start();
    }

    public void stopRecording() {
        recordingStatus = false;
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        camera.lock();
        camera.release();
    }

    public void generateHash(){
        int[] orderOfVideo = new int[3];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if(accidentOnVideoIndex == 2){
            orderOfVideo[0] = 1;
            orderOfVideo[1] = 2;
        }else{
            orderOfVideo[0] = 2;
            orderOfVideo[1] = 1;
        }
        orderOfVideo[2] = 3;

        File dir = new File(Environment.getExternalStorageDirectory().toString()+"/dashitHistory/"+ DateFormat.format("dd-MM-yyyy HH:mm", new Date().getTime()));
        if(!dir.isDirectory())
            dir.mkdirs();
        File saveFile;
        for(int i=0;i<3;i++){
            File file = new File(Environment.getExternalStorageDirectory().toString()+"/dashit"+orderOfVideo[i]+".mp4");
            if(file.exists() && !file.isDirectory()){
                byte[] byteArray = new byte[(int)file.length()];
                InputStream fileInputStream;
                try {
                    fileInputStream = new FileInputStream(file);
                    fileInputStream.read(byteArray);
                    Log.i("File length::", ""+byteArray.length);
                    outputStream.write(byteArray);
                    fileInputStream.close();

                    fileInputStream = new FileInputStream(file);
                    saveFile = new File(dir.getPath()+"/"+(i+1)+"accVideo"+orderOfVideo[i]+".mp4");
                    OutputStream out = new FileOutputStream(saveFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = fileInputStream.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    fileInputStream.close();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
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
            for (int i=0;i<mdBytes.length;i++) {
                hexString.append(Integer.toHexString(0xFF & mdBytes[i]));
            }
            Log.i("Hex format : ", "" + hexString.toString());

            //Create a hash.txt file
            File hash = new File(dir.getPath()+"/hash.txt");
            FileWriter writer = new FileWriter(hash);
            writer.append(hexString.toString());
            writer.flush();
            writer.close();

            sendHashToServer(hexString.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHashToServer(String hashString){
        String url = "http://www.originstamp.org/api/stamps";
        String postData = "{\"hash_sha256\" : \""+hashString+"\"}";
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Token token=\"a876e0bbb8894e8c8eadc5b3a19adff7\"");
            con.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
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
                    Toast.makeText(BackgroundService.this, "Hash sent successfully!", Toast.LENGTH_LONG).show();
                    windowManager.removeView(surfaceView);
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
    public void surfaceCreated(SurfaceHolder holder) {
        globalHolder = holder;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!accidentStatus){
                    int i =1;
                    while(i < 3) {
                        if (!accidentStatus && !manualStopStatus) {
                            accidentOnVideoIndex = i;
                            startRecording("dashit" + i);
                            try {
                                Thread.sleep(10000);
                                if(recordingStatus)
                                    stopRecording();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            i++;
                        }else{
                            break;
                        }
                    }
                }
                if(!manualStopStatus) {

                    startRecording("dashit3");
                    try {
                        Thread.sleep(10000);
                        stopRecording();
                        handler.removeCallbacks(mStatusChecker);
                        generateHash();
                        System.exit(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    public static class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            accidentStatus = true;
        }
    }
}
