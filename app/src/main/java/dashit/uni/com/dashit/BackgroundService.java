/**
 * Created by Jagrut on 23-Jan-16.
 */

package dashit.uni.com.dashit;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class BackgroundService extends IntentService {

    public BackgroundService(){
        super("BackgroundService");
    }

    private static final String TAG = "RecorderService";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private static Camera mServiceCamera;
    private boolean mRecordingStatus;
    private MediaRecorder mMediaRecorder;
    static boolean accidentStatus = false;
    boolean manualStopStatus = false;
    int accidentOnVideoIndex = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        manualStopStatus = true;
        if(mRecordingStatus){
            stopRecording();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while(!accidentStatus){
            int i =1;
            while(i < 3) {
                if (!accidentStatus && !manualStopStatus) {
                    accidentOnVideoIndex = i;
                    //mServiceCamera = CameraRecorder.mCamera;
                    //mServiceCamera = Camera.open(1);
                    mSurfaceView = HomeActivity.mSurfaceView;
                    mSurfaceHolder = HomeActivity.mSurfaceHolder;
                    startRecording("dashit" + i);
                    try {
                        Thread.sleep(5000);
                        if(mRecordingStatus)
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
            mSurfaceView = HomeActivity.mSurfaceView;
            mSurfaceHolder = HomeActivity.mSurfaceHolder;
            startRecording("dashit3");
            try {
                Thread.sleep(5000);
                stopRecording();
                generateHash();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean startRecording(String fileName){
        try {
            //Toast.makeText(getBaseContext(), "Recording Started", Toast.LENGTH_SHORT).show();
            mRecordingStatus = true;
            mServiceCamera = Camera.open();
            Camera.Parameters params = mServiceCamera.getParameters();
            mServiceCamera.setParameters(params);
            Camera.Parameters p = mServiceCamera.getParameters();

            final List<Camera.Size> listSize = p.getSupportedPreviewSizes();
            Camera.Size mPreviewSize = listSize.get(2);
            Log.v(TAG, "use: width = " + mPreviewSize.width
                    + " height = " + mPreviewSize.height);
            p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
            mServiceCamera.setParameters(p);
            mServiceCamera.setDisplayOrientation(90);
            try {
                mServiceCamera.setPreviewDisplay(mSurfaceHolder);
                mServiceCamera.startPreview();
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            mServiceCamera.unlock();

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
            //mMediaRecorder.setMaxDuration(10000);
            mMediaRecorder.setOutputFile("/sdcard/" + fileName + ".mp4");

            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            return true;
        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void stopRecording() {
        //Toast.makeText(getBaseContext(), "Recording Stopped", Toast.LENGTH_SHORT).show();
        mRecordingStatus = false;
        try {
            mServiceCamera.reconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        mServiceCamera.stopPreview();
        mMediaRecorder.release();

        mServiceCamera.release();
        mServiceCamera = null;
    }

    public void generateHash(){
        int[] orderOfVideo = new int[3];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if(accidentOnVideoIndex == 2){
            orderOfVideo[0] = 1;
            orderOfVideo[1] = 2;
            orderOfVideo[2] = 3;
        }else{
            orderOfVideo[0] = 2;
            orderOfVideo[1] = 1;
            orderOfVideo[2] = 3;
        }
        for(int i=0;i<3;i++){
            File file = new File("/sdcard/dashit"+orderOfVideo[i]+".mp4");
            if(file.exists() && !file.isDirectory()){
                byte[] byteArray = new byte[(int)file.length()];
                FileInputStream fileInputStream;
                try {
                    fileInputStream = new FileInputStream(file);
                    fileInputStream.read(byteArray);
                    System.out.println("File length::"+byteArray.length);
                    outputStream.write(byteArray);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        byte[] finalByte = outputStream.toByteArray();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(finalByte);
            byte[] mdBytes = md.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<mdBytes.length;i++) {
                hexString.append(Integer.toHexString(0xFF & mdBytes[i]));
            }
            System.out.println("Hex format : " + hexString.toString());
            sendHashToServer(hexString.toString());
        } catch (NoSuchAlgorithmException e) {
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
            con.setRequestProperty("Accept","*/*");
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Accident!!");
            accidentStatus = true;
        }
    }
}
