package dashit.uni.com.dashit;

/**
 * Created by Jagrut on 23-Jan-16.
 */

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView txtView;
    Intent intent;
    MyResultReceiver resultReceiver;
    Button button;

    //Camera variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Camera code starts
        //Camera code ends

        resultReceiver = new MyResultReceiver(null);
        txtView = (TextView)findViewById(R.id.accData);

        intent = new Intent(this, BackgroundService.class );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("receiver", resultReceiver);
        startService(intent);
        //finish();
        button = (Button)findViewById(R.id.stopRecording);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.exit(0);
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
