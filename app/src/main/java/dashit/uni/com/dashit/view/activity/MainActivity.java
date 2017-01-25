package dashit.uni.com.dashit.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import dashit.uni.com.dashit.DashItApplication;
import dashit.uni.com.dashit.R;
import dashit.uni.com.dashit.service.BackgroundService;
import dashit.uni.com.dashit.service.SensorService;

/**
 * Created by Jagrut on 23-Jan-16.
 * The Video Recording Activity. All monitoring services are triggered from this activity.
 * View after the user hits 'Record' icon.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback,
        LocationListener {

    private static ImageView imgView;

    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private GoogleMap googleMap;
    private LatLng latLng;
    private SupportMapFragment mapFragment;
    private Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = (ImageView) findViewById(R.id.status);

        //Start the accelerometer monitoring
        Intent sensorIntent = new Intent(MainActivity.this, SensorService.class);
        sensorIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(sensorIntent);

        //Start video recording in background
        Intent backgroundIntent = new Intent(MainActivity.this, BackgroundService.class);
        backgroundIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(backgroundIntent);

        FloatingActionButton btnStop = (FloatingActionButton) findViewById(R.id.fab_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, BackgroundService.class));
                stopService(new Intent(MainActivity.this, SensorService.class));
                System.exit(0);
            }
        });
        registerReceiver(collisionBroadcastReceiver, new IntentFilter("COLLISION_DETECTED_INTERNAL"));
        Snackbar.make(findViewById(android.R.id.content), "You can switch application", Snackbar.LENGTH_LONG).show();

        // Create the LocationRequest object
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);

        //Create Map Fragment to display Google Maps
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(collisionBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopService(new Intent(MainActivity.this, BackgroundService.class));
        stopService(new Intent(MainActivity.this, SensorService.class));
        System.exit(0);
    }

    /**
     * Connect to LocationService to get the location
     */
    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setMyLocationEnabled(true);
        buildGoogleApiClient();
        googleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Set up map with current location
     */
    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (lastLocation != null) {
            //If everything went fine lets get latitude and longitude
            latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            currentLocationMarker = googleMap.addMarker(markerOptions);
        }

        locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Change the position of marker on map when location is changed.
     */
    @Override
    public void onLocationChanged(Location location) {
        if(currentLocationMarker != null){
            currentLocationMarker.remove();
        }

        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationMarker = googleMap.addMarker(markerOptions);

        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(14).build();

        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /**
     * Ask for user confirmation when collision is detected using accelerometer and location change services.
     * Accordingly, trigger the appropriate tasks.
     * If no user input received within 10 seconds, trigger a confirmation of collision and respective tasks.
     */
    BroadcastReceiver collisionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            confirmDialogBuilder.setMessage("Was this a collision?");
            confirmDialogBuilder.setCancelable(false);
            confirmDialogBuilder.setTitle("Please confirm");

            confirmDialogBuilder.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        //Confirm collision and then notify BackgroundService of the same
                        public void onClick(DialogInterface dialog, int id) {
                            Intent collisionConfirmedIntent = new Intent();
                            collisionConfirmedIntent.setAction("com.collisionConfirmed.Broadcast");
                            collisionConfirmedIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            collisionConfirmedIntent.putExtra("accidentLocation", (String) intent.getExtras().get("accidentLocation"));
                            sendBroadcast(collisionConfirmedIntent);
                            dialog.cancel();
                        }
                    });

            confirmDialogBuilder.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        //False Positive
                        public void onClick(DialogInterface dialog, int id) {
                            imgView.setImageResource(R.drawable.green_dot);
                            dialog.cancel();
                        }
                    });

            final AlertDialog confirmDialog = confirmDialogBuilder.create();
            confirmDialog.setCanceledOnTouchOutside(false);
            confirmDialog.setCancelable(false);
            confirmDialog.show();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(confirmDialog.isShowing()){
                        //No User Input. Confirm Collision Automatically.
                        confirmDialog.cancel();
                        Intent collisionConfirmedIntent = new Intent();
                        collisionConfirmedIntent.setAction("com.collisionConfirmed.Broadcast");
                        collisionConfirmedIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        collisionConfirmedIntent.putExtra("accidentLocation", (String) intent.getExtras().get("accidentLocation"));
                        sendBroadcast(collisionConfirmedIntent);
                    }

                }
            }, 10 * 1000);

        }
    };

    /**
     * Steps to take in case a collision is detected, notified from LocationChangeService.
     */
    public static class CollisionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            imgView.setImageResource(R.drawable.carcollision);
            Intent collisionDetectionFromLocation = new Intent("COLLISION_DETECTED_INTERNAL");
            collisionDetectionFromLocation.putExtra("accidentLocation", (String) intent.getExtras().get("accidentLocation"));
            context.sendBroadcast(collisionDetectionFromLocation);
        }


    }
}