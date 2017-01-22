package dashit.uni.com.dashit.view.activity;

/**
 * Created by Jagrut on 23-Jan-16.
 * The Video Recording Activity. All monitoring services are triggered from this activity.
 * View after the user hits 'Record' icon.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
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


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback,
        LocationListener {

    private static ImageView imgView;

    private static double latToSend;
    private static double longToSend;

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

        Intent intent2 = new Intent(MainActivity.this, SensorService.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent2);

        Intent intent = new Intent(MainActivity.this, BackgroundService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);

        FloatingActionButton btnStop = (FloatingActionButton) findViewById(R.id.fab_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, BackgroundService.class));
                stopService(new Intent(MainActivity.this, SensorService.class));
                System.exit(0);
            }
        });

        Snackbar.make(findViewById(android.R.id.content), "You can switch application", Snackbar.LENGTH_LONG).show();

        // Create the LocationRequest object
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

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

            latToSend = lastLocation.getLatitude();
            longToSend = lastLocation.getLongitude();
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

        latToSend = location.getLatitude();
        longToSend = location.getLongitude();
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

    static int smsCount = 0;

    /**
     * Steps to take in case a collision is detected, notified from SensorService.
     * Send an SMS containing collision location, if the user has selected for it.
     */
    public static class CollisionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            imgView.setImageResource(R.drawable.carcollision);
            if (smsCount == 0) {
                sendMessage();
                smsCount++;
            }
        }

        public void sendMessage() {
            //Get data from preferences
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(DashItApplication.getAppContext());
            if (SP.getBoolean("sendSms", false)) {
                String myName = SP.getString("myName", "NA");
                String myPhoneNumber = SP.getString("myPhoneNumber", "NA");
                String emergencyContact = SP.getString("contact", "NA");
                String location = "http://maps.google.com/?q=";
                location += latToSend + "," + longToSend;
                String message = "Your contact: " + myName + "\n needs urgent help!" +
                        "\n Phone number: " + myPhoneNumber + "," +
                        "\n Current location: " + location;

                System.out.println(message);
                //Send SMS
                if (emergencyContact.length() > 2 && !emergencyContact.equalsIgnoreCase("NA")) {
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(emergencyContact, null, message, null, null);
                        Toast.makeText(DashItApplication.getAppContext(), "SMS Sent!",
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}