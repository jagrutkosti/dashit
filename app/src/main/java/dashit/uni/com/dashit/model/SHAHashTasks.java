package dashit.uni.com.dashit.model;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import dashit.uni.com.dashit.DashItApplication;
import dashit.uni.com.dashit.R;

/**
 * Created by Jagrut on 25-Jan-17.
 */

public class SHAHashTasks {
    /**
     * Create a byte array of video files in given directory and location and then generate hash.
     */
    public static String generateHashFromFilesAndLocation(String directory, String location){
        String generateHashFromFileAndLocation = null;
        File directoryOfFiles = new File(directory);
        if(directoryOfFiles.isDirectory()){
            File[] filesInDir = directoryOfFiles.listFiles();
            Arrays.sort(filesInDir);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for(File file : filesInDir){
                if(file.exists() && !file.isDirectory() && file.getAbsolutePath().endsWith(".mp4")){
                    //byte[] byteArray = new byte[(int) file.length()];
                    try {
                        InputStream fileIS = new FileInputStream(file);
                        BufferedInputStream bufferedIS = new BufferedInputStream(fileIS);
                        byte buffer[] = new byte[1024];
                        int read;
                        while((read = bufferedIS.read(buffer)) != -1){
                            outputStream.write(buffer, 0, read);
                        }
                        /*fileIS.read(byteArray);
                        outputStream.write(byteArray);*/
                        fileIS.close();
                        bufferedIS.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                outputStream.write(location.getBytes());
                byte[] finalByte = outputStream.toByteArray();
                Log.i("Final Byte Array Length", "" + finalByte.length);

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(finalByte);
                byte[] mdBytes = md.digest();
                StringBuffer hexString = new StringBuffer();
                for (int i = 0; i < mdBytes.length; i++) {
                    hexString.append(Integer.toHexString(0xFF & mdBytes[i]));
                }
                Log.i("Hex format : ", "" + hexString.toString());
                outputStream.close();
                generateHashFromFileAndLocation = hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return generateHashFromFileAndLocation;
    }

    /**
     * Fetches data from origin stamp server for the given Hash value
     * @param hash the hash for which data should be fetched
     * @return {String} the data from server in String
     */
    public static String getDataForHash(String hash){
        String url = DashItApplication.getAppContext().getString(R.string.timestampUrl);
        StringBuffer buffer = new StringBuffer();
        try {
            URL obj = new URL(url + hash);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Token token=\"" + DashItApplication.getAppContext().getString(R.string.timestampToken) + "\"");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            con.setRequestProperty("Accept", "*/*");
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return buffer.toString();
    }
}
