package dashit.uni.com.dashit.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Jagrut on 24-Jan-17.
 * If a collision is detected by the Accelerometer sensor, check if the device location changes
 * at least 50 meters in the next 10 seconds.
 */

public class LocationChangeService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient googleApiClient;
    private Location currentLocation;

    /**
     * Connect to LocationService to get the current location of the device.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if(googleApiClient == null){
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleApiClient.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Perform the waiting task in asynchronous mode
        new WaitForLocationCheck().execute();
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Asynchronously wait for 10 seconds and again get the location to check if it has changed.
     */
    public class WaitForLocationCheck extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if(lastLocation != null){
                currentLocation = lastLocation;
                try {
                    //Wait for 10 seconds and again get the location and consecutively, distance
                    Thread.sleep(10000);
                    float distanceInMeters = lastLocation.distanceTo(currentLocation);

                    if(distanceInMeters < 50.00){
                        String accidentLocation = String.valueOf(lastLocation.getLatitude());
                        accidentLocation = accidentLocation + "," + String.valueOf(lastLocation.getLongitude());

                        Intent collisionDetectedIntent = new Intent();
                        collisionDetectedIntent.setAction("com.collisionDetected.Broadcast");
                        collisionDetectedIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        collisionDetectedIntent.putExtra("accidentLocation", accidentLocation);
                        sendBroadcast(collisionDetectedIntent);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
