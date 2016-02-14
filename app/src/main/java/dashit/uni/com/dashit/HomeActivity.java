package dashit.uni.com.dashit;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    //Camera
    TextView txtView;
    MyResultReceiver resultReceiver;

    private static final String TAG = "Recorder";
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Camera
        resultReceiver = new MyResultReceiver(null);
        txtView = (TextView)findViewById(R.id.accData);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        Button btnStart = (Button) findViewById(R.id.start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, BackgroundService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(intent);

                Intent intent2 = new Intent(HomeActivity.this, SensorService.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra("receiver", resultReceiver);
                startService(intent2);
            }
        });

        Button btnStop = (Button) findViewById(R.id.stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(HomeActivity.this, BackgroundService.class));
                stopService(new Intent(HomeActivity.this, SensorService.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    class MyResultReceiver extends ResultReceiver{

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public MyResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            if(resultCode == 100){
                runOnUiThread(new UpdateUI(resultData.getString("state")));
            }
        }
    }

    class UpdateUI implements Runnable{
        String updateString;

        public UpdateUI(String updateString) {
            this.updateString = updateString;
        }
        public void run() {
            if(updateString.equalsIgnoreCase("Accident!!!"))
                txtView.setTextColor(Color.parseColor("red"));
            else
                txtView.setTextColor(Color.parseColor("black"));
            txtView.setText(updateString);
        }
    }

}
