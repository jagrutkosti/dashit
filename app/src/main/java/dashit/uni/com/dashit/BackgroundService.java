/**
 * Created by Jagrut on 23-Jan-16.
 */

package dashit.uni.com.dashit;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    static boolean status = false;

    @Override
    public void onCreate() {
        /*mRecordingStatus = false;
        //mServiceCamera = CameraRecorder.mCamera;
        mServiceCamera = Camera.open(1);
        mSurfaceView = HomeActivity.mSurfaceView;
        mSurfaceHolder = HomeActivity.mSurfaceHolder;*/

        super.onCreate();
    }

    @Override
        public void onDestroy() {
        //stopRecording();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while(!status){
            /*File file;
            file = new File("/sdcard/dashit1.mp4");
            file.delete();
            file = new File("/sdcard/dashit2.mp4");
            file.delete();
            file = new File("/sdcard/dashit3.mp4");
            file.delete();*/

            int i =1;
            while(i < 4) {
                if (!status) {
                    //mServiceCamera = CameraRecorder.mCamera;
                    //mServiceCamera = Camera.open(1);
                    mSurfaceView = HomeActivity.mSurfaceView;
                    mSurfaceHolder = HomeActivity.mSurfaceHolder;
                    startRecording("dashit" + i);
                    try {
                        Thread.sleep(5000);
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
        mSurfaceView = HomeActivity.mSurfaceView;
        mSurfaceHolder = HomeActivity.mSurfaceHolder;
        startRecording("dashit4");
        try {
            Thread.sleep(5000);
            stopRecording();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean startRecording(String fileName){
        try {
            //Toast.makeText(getBaseContext(), "Recording Started", Toast.LENGTH_SHORT).show();

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

    public static class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Accident!!");
            status = true;
        }
    }
}
