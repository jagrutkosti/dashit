/**
 * Created by Jagrut on 23-Jan-16.
 */

package dashit.uni.com.dashit;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

public class BackgroundService extends Service implements SensorEventListener {

    private SensorManager manager = null;
    private Sensor sensor = null;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;

    ResultReceiver resultReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        resultReceiver = intent.getParcelableExtra("receiver");

        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        boolean sensorAvailable = manager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_FASTEST);
        if(sensorAvailable){
            System.out.println("Sensor available");
        }else{
            System.out.println("Some problem when retrieving the sensor.");
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            /*float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;*/


            if(Math.abs(x-last_x) > 8 || Math.abs(y-last_y) > 8 || Math.abs(z-last_z) > 8){
                Bundle bundle  = new Bundle();
                bundle.putString("state","Accident!!!");
                resultReceiver.send(100,bundle);
            }else{
                Bundle bundle  = new Bundle();
                bundle.putString("state","You are good!!!");
                resultReceiver.send(100,bundle);
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

    public class SensorEventLoggerTask extends AsyncTask<SensorEvent, Void, Void>{
        @Override
        public Void doInBackground(SensorEvent... events) {
            for(int i=0;i<events.length;i++){
                SensorEvent event = events[i];
                for(int j=0;j<event.values.length;j++){
                    System.out.println("Sensor Data::"+event.values[i]);
                }
            }
            return null;
        }
    }


}
