package dashit.uni.com.dashit.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import dashit.uni.com.dashit.DashItApplication;
import dashit.uni.com.dashit.R;
import dashit.uni.com.dashit.helper.SHAHashTasks;

/**
 * Created by Jagrut on 25-Jan-17.
 * Tasks to be performed upon detection  and confirmation of collision and after recording has stopped.
 */

public class PostCollisionTasksService extends Service {
    private Handler handler;
    private String hashFromFilesAndLocation;
    private String accidentLocation;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();

        Bundle paramFromServiceInvocation = intent.getExtras();
        String directoryPath = (String) paramFromServiceInvocation.get("directoryPath");
        accidentLocation = (String) intent.getExtras().get("accidentLocation");
        hashFromFilesAndLocation = SHAHashTasks.generateHashFromFilesAndLocation(directoryPath, accidentLocation);

        if(hashFromFilesAndLocation != null && hashFromFilesAndLocation.length() > 0){
            //Create a hash.txt file
            File hash = new File(intent.getExtras().get("directoryPath") + "/hash.txt");
            FileWriter writerHash = null;
            try {
                writerHash = new FileWriter(hash);
                writerHash.append(hashFromFilesAndLocation);
                writerHash.flush();
                writerHash.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Create location.txt file
            File location = new File(intent.getExtras().get("directoryPath") + "/location.txt");
            FileWriter writerLocation = null;
            try {
                writerLocation = new FileWriter(location);
                writerLocation.append(accidentLocation);
                writerLocation.flush();
                writerLocation.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new SendHashToServer().execute(hashFromFilesAndLocation);
        } else{
            Log.e("PostCollision:", "Generated hash is null.");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Send the generated hash to Originstamp server, asynchronously
     */
    public class SendHashToServer extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            String url = getString(R.string.timestampUrl);
            String postData = "{\"hash_sha256\" : \"" + strings[0] + "\"}";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", "Token token=\""+getString(R.string.timestampToken)+"\"");
                con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                con.setRequestProperty("Accept", "*/*");
                DataOutputStream dos = new DataOutputStream(con.getOutputStream());
                dos.writeBytes(postData);
                dos.flush();
                dos.close();
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    Log.i("Server Data:",line);
                }
                reader.close();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DashItApplication.getAppContext(), R.string.instruction_hash_sent_successful, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DashItApplication.getAppContext(), R.string.instruction_hash_sent_unsuccessful, Toast.LENGTH_LONG).show();
                    }
                });
            }
            sendMessage();
            return null;
        }

        /**
         * Send message to emergency contact f the user has chosen to do so in SettingsActivity
         * Data sent: App User's Name, Phone Number and Accident Location
         */
        public void sendMessage() {
            //Get data from preferences
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(DashItApplication.getAppContext());
            if (SP.getBoolean("sendSms", false)) {
                String myName = SP.getString("myName", "NA");
                String myPhoneNumber = SP.getString("myPhoneNumber", "NA");
                String emergencyContact = SP.getString("contact", "NA");
                String location = "http://maps.google.com/?q=";
                location += accidentLocation;
                String message = getString(R.string.sms_message_contact) + myName + getString(R.string.sms_message_help) +
                        getString(R.string.sms_message_your_phone_number) + myPhoneNumber + "," +
                        getString(R.string.sms_message_location) + location;
                String hashMessage = "SHA-256 Hash:" + hashFromFilesAndLocation;
                //Send SMS
                if (emergencyContact.length() > 2 && !emergencyContact.equalsIgnoreCase("NA")) {
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(emergencyContact, null, message, null, null);
                        smsManager.sendTextMessage(emergencyContact, null, hashMessage, null, null);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DashItApplication.getAppContext(), R.string.instruction_sms_sent_successful, Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DashItApplication.getAppContext(), R.string.instruction_sms_sent_unsuccessful, Toast.LENGTH_LONG).show();
                            }
                        });
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
