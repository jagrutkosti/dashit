package dashit.uni.com.dashit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jagrut on 20-Nov-16.
 */
public class tempTest {

    public static void main(String args[]){
        sendHashToServer("c89fe15f1b200316b669eb9901b7faaf733ba28ba5989d1c3527161b786a0043");
    }

    public static void sendHashToServer(String hashString) {
        String url = "http://www.originstamp.org/api/stamps/";
        String postData = "{" + hashString + "}";
        try {
            URL obj = new URL(url+hashString);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Token token=\"a876e0bbb8894e8c8eadc5b3a19adff7\"");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            con.setRequestProperty("Accept", "*/*");
            int responseCode = con.getResponseCode();
            System.out.println(responseCode);
            String line;
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
            System.out.println("Response:::"+buffer.toString());

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}


