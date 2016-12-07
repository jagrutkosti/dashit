package dashit.uni.com.dashit;

/**
 * Created by Jagrut on 23-Jan-16.
 * The Video Recording Activity. All monitoring services are triggered from this activity.
 * View after the user hits 'Record' icon.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static TextView txtView;
    private static ImageView imgView;

    private static double latToSend;
    private static double longToSend;

    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView = (TextView)findViewById(R.id.accData);
        imgView = (ImageView)findViewById(R.id.status);

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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);
    }

    /**
     * The method that adds a Map into this Activity. Removed because of un-necessity.
     * Code kept for future reference, if requirements change.
     */
    /*private void initializeMap() {
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
            googleMap.getUiSettings().setAllGesturesEnabled(true);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            googleMap.setMyLocationEnabled(true);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(currentLatitude, currentLongitude))      // Sets the center of the map to Mountain View
                    .zoom(17)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)));
        }
    }*/

    @Override
    public void onBackPressed() { }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            latToSend = currentLatitude;
            longToSend = currentLongitude;
            //initializeMap();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        latToSend = currentLatitude;
        longToSend = currentLongitude;
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
    public static class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            txtView.setTextColor(Color.parseColor("red"));
            imgView.setImageResource(R.drawable.carcollision);
            txtView.setText(R.string.collision);
            if(smsCount == 0){
                sendMessage();
                smsCount++;
            }
        }

        public void sendMessage(){
            //Get data from preferences
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
            if(SP.getBoolean("sendSms", false)) {
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
                        Toast.makeText(MyApplication.getAppContext(), "SMS Sent!",
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}