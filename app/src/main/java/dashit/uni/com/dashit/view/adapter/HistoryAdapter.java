package dashit.uni.com.dashit.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import dashit.uni.com.dashit.R;
import dashit.uni.com.dashit.model.HistoryFiles;

/**
 * Created by Jagrut on 29-Nov-16.
 * The Adapter that links the video folder information and list view for showing the History
 */

public class HistoryAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflator;
    private ArrayList<HistoryFiles> dataSource;
    private int size;

    public HistoryAdapter(Context context, ArrayList<HistoryFiles> dataSource){
        this.context = context;
        this.dataSource = dataSource;
        inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        size = this.dataSource.size();
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = inflator.inflate(R.layout.list_item, parent, false);
        if(position % 2 == 0)
            rowView.setBackgroundColor(Color.parseColor("#FFFFBB33"));
        TextView textLabel = (TextView) rowView.findViewById(R.id.lblListItem);
        ImageView hashStatus = (ImageView) rowView.findViewById(R.id.hashStatus);

        HistoryFiles directory = (HistoryFiles) getItem(position);
        textLabel.setText(directory.getDirectory());

        if(directory.getTxHash() != null && directory.getTxHash().length() > 0)
            hashStatus.setImageResource(R.drawable.seed_submitted);
        else
            hashStatus.setImageResource(R.drawable.seed_not_submitted);

        List<String> files = directory.getFilesInDirectory();
        for(String fileName : files){
            if(fileName.endsWith(".txt")){
                File hashFile = new File(fileName);
                StringBuilder hash = new StringBuilder();
                try{
                    BufferedReader br = new BufferedReader(new FileReader(hashFile));
                    String line;
                    while((line = br.readLine()) != null){
                        hash.append(line);
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(!directory.isCheckedAtServer()) {
                    directory.setCheckedAtServer(true);
                    directory.setSavedHash(hash.toString());
                    new HashStatusCheck(this).execute(hash.toString(), directory.getDirectory());
                }
            }
        }
        return rowView;
    }

    /**
     * Asynchronously check if hash was submitted to Bitcoin Blockchain or not
     * Accordingly update the List view for 'History'
     */
    private class HashStatusCheck extends AsyncTask<String, String, String>{

        private String directory;
        private HistoryAdapter adapter;

        HashStatusCheck(HistoryAdapter adapter){
            this.adapter = adapter;
        }

        @Override
        protected String doInBackground(String... hashString) {
            directory = hashString[1];
            String url = "http://www.originstamp.org/api/stamps/";
            StringBuffer buffer = new StringBuffer();
            try {
                URL obj = new URL(url+hashString[0]);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", "Token token=\"a876e0bbb8894e8c8eadc5b3a19adff7\"");
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

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                if(jsonObject.has("blockchain_transaction")) {
                    JSONObject blockchainTran = jsonObject.getJSONObject("blockchain_transaction");
                    for (HistoryFiles file : dataSource) {
                        if (file.getDirectory().equalsIgnoreCase(directory)) {
                            if (blockchainTran.has("tx_hash"))
                                file.setTxHash(blockchainTran.getString("tx_hash"));
                            if (blockchainTran.has("recipient"))
                                file.setRecipient(blockchainTran.getString("recipient"));
                            if (blockchainTran.has("updated_at"))
                                file.setSubmissionTime(blockchainTran.getString("updated_at"));
                            if (blockchainTran.has("private_key"))
                                file.setPrivateKey(blockchainTran.getString("private_key"));
                            if (blockchainTran.has("public_key"))
                                file.setPublicKey(blockchainTran.getString("public_key"));
                            if (blockchainTran.has("seed"))
                                file.setSeed(blockchainTran.getString("seed"));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
