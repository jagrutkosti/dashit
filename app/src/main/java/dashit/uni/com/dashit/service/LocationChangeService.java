package dashit.uni.com.dashit.service;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import dashit.uni.com.dashit.helper.LocationAssistant;

/**
 * Created by Jagrut on 24-Jan-17.
 * If a collision is detected by the Accelerometer sensor, check if the device location changes
 * at least 50 meters in the next 10 seconds.
 */

public class LocationChangeService extends Service implements LocationAssistant.Listener{

    private Location firstLocation;
    private LocationAssistant locationAssistant;
    int count = 0;
    boolean tenSecondsElapsed = false;
    /**
     * Connect to LocationAssistant to get the current location of the device and do not allow mock locations.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        locationAssistant = new LocationAssistant(this, this, LocationAssistant.Accuracy.HIGH, 500, false);
        locationAssistant.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationAssistant.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onNeedLocationPermission() {

    }

    @Override
    public void onExplainLocationPermission() {

    }

    @Override
    public void onLocationPermissionPermanentlyDeclined(View.OnClickListener fromView, DialogInterface.OnClickListener fromDialog) {

    }

    @Override
    public void onNeedLocationSettingsChange() {

    }

    @Override
    public void onFallBackToSystemSettings(View.OnClickListener fromView, DialogInterface.OnClickListener fromDialog) {

    }

    /**
     * Called when a new location is available.
     * @param lastLocation The last known  location of the device
     */
    @Override
    public void onNewLocationAvailable(Location lastLocation) {
        if(lastLocation != null && count == 0){
            //For first location, initiate counter and save this location
            firstLocation = lastLocation;
            new WaitForLocationCheck().execute();
        }
        count++;
        checkDistance(lastLocation);
    }

    @Override
    public void onMockLocationsDetected(View.OnClickListener fromView, DialogInterface.OnClickListener fromDialog) {

    }

    @Override
    public void onError(LocationAssistant.ErrorType type, String message) {

    }

    /**
     * Check the distance from first known location with last known location.
     * If distance is less than 50 meters, initiate a collisionDetected broadcast.
     * @param lastLocation The last known location received from LocationAssistant
     */
    public void checkDistance(Location lastLocation) {
        if(tenSecondsElapsed){
            float distanceInMeters = lastLocation.distanceTo(firstLocation);
            if(distanceInMeters < 50.00){
                String accidentLocation = String.valueOf(lastLocation.getLatitude());
                accidentLocation = accidentLocation + "," + String.valueOf(lastLocation.getLongitude());

                Intent collisionDetectedIntent = new Intent();
                collisionDetectedIntent.setAction("com.collisionDetected.Broadcast");
                collisionDetectedIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                collisionDetectedIntent.putExtra("accidentLocation", accidentLocation);
                sendBroadcast(collisionDetectedIntent);
                stopSelf();
            }
        }
    }

    /**
     * Asynchronously wait for 10 seconds and again get the location to check if it has changed.
     */
    public class WaitForLocationCheck extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(10000);
                tenSecondsElapsed = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
