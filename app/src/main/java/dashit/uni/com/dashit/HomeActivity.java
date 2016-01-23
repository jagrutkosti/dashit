package dashit.uni.com.dashit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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

    public void startObservation(View view){
        System.out.println("Button Clicked!");

      /*  Intent intent = new Intent(HomeActivity.this, BackgroundService.class );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);*/

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();

       /* AlarmManager scheduler = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), BackgroundService.class );
        PendingIntent scheduledIntent = PendingIntent.getService(getApplicationContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        scheduler.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                3000, scheduledIntent);*/

    }
}
