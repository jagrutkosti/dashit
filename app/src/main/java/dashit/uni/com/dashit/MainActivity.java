package dashit.uni.com.dashit;

/**
 * Created by Jagrut on 23-Jan-16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    static TextView txtView;
    static ImageView imgView;

    private static final String TAG = "Recorder";
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView = (TextView)findViewById(R.id.accData);
        imgView = (ImageView)findViewById(R.id.likeacc);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Intent intent2 = new Intent(MainActivity.this, SensorService.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent2);

        Intent intent = new Intent(MainActivity.this, BackgroundService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);

        ImageButton btnStop = (ImageButton) findViewById(R.id.stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, BackgroundService.class));
                stopService(new Intent(MainActivity.this, SensorService.class));
                System.exit(0);
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

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
            System.out.println("Activity Collision!!");
            txtView.setTextColor(Color.parseColor("red"));
            imgView.setBackgroundResource(R.drawable.carcollision);
            txtView.setText("Collision!!");
        }
    }
}