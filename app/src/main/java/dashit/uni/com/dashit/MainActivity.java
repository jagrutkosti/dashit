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
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity{

    static TextView txtView;
    static ImageView imgView;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView = (TextView)findViewById(R.id.accData);
        imgView = (ImageView)findViewById(R.id.likeacc);

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

        try {
            initializeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.setMyLocationEnabled(true);
            MapsInitializer.initialize(this);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(47.66, 9.18), 10);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeMap();
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