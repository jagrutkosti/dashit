package dashit.uni.com.dashit.service;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Jagrut on 13-Feb-16.
 * Service to check for collision. Utilized Accelerometer of the smart-phone
 */
public class SensorService extends IntentService implements SensorEventListener {

    private long lastUpdate = 0;
    private float last_x = 6.0f, last_y = 6.0f, last_z = 6.0f;


    public SensorService() {
        super("SensorService");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 100) {
            lastUpdate = curTime;

            if(Math.abs(x-last_x) > 8 || Math.abs(y-last_y) > 8 || Math.abs(z-last_z) > 8){
                Intent intent = new Intent();
                intent.setAction("com.example.Broadcast");
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(intent);
            }

            last_x = x;
            last_y = y;
            last_z = z;
        }
        new SensorEventLoggerTask().execute(event);
        stopSelf();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SensorManager manager = null;
        Sensor sensor = null;
        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public class SensorEventLoggerTask extends AsyncTask<SensorEvent, Void, Void> {
        @Override
        public Void doInBackground(SensorEvent... events) {
            return null;
        }
    }
}
