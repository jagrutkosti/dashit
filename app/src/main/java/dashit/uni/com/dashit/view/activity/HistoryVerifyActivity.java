package dashit.uni.com.dashit.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dashit.uni.com.dashit.R;
import dashit.uni.com.dashit.model.HistoryFiles;
import dashit.uni.com.dashit.view.adapter.HistoryAdapter;

/**
 * Created by Jagrut on 29-Nov-16.
 * The Activity which is shown after the user taps on 'History' from Menu item
 */

public class HistoryVerifyActivity extends AppCompatActivity {
    private ListView mListView;
    private ArrayList<HistoryFiles> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_verify);

        mListView = (ListView) findViewById(R.id.dates);
        populateView();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryFiles file = itemList.get(position);
                Intent verifyActivity = new Intent(getApplicationContext(), VerifyFilesActivity.class);
                verifyActivity.putExtra("directory", file.getDirectory());
                for(int i = 0; i < file.getFilesInDirectory().size(); i++){
                    if(file.getFilesInDirectory().get(i).endsWith(".mp4"))
                        verifyActivity.putExtra("file"+i, file.getFilesInDirectory().get(i));
                }
                verifyActivity.putExtra("tx_hash", file.getTxHash());
                verifyActivity.putExtra("seed", file.getSeed());
                verifyActivity.putExtra("savedHash", file.getSavedHash());
                startActivity(verifyActivity);
            }
        });
    }

    /**
     * Populating the list view using the HistoryAdapter
     */
    public void populateView(){
        File rootDirectory = new File(Environment.getExternalStorageDirectory().toString()+"/dashitHistory/");
        if(rootDirectory.exists()){
            File[] directories = new File(rootDirectory.getPath()).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            for (File dir : directories) {
                HistoryFiles files = new HistoryFiles();
                files.setDirectory(dir.getPath().substring(dir.getPath().lastIndexOf("/") + 1));
                List<String> fileNames = new ArrayList<>();
                if(dir.isDirectory()){
                    File[] filesInDir = dir.listFiles();
                    Arrays.sort(filesInDir);
                    for(File f : filesInDir){
                        fileNames.add(f.getAbsolutePath());
                    }
                }
                files.setFilesInDirectory(fileNames);
                itemList.add(files);
            }
            HistoryAdapter adapter = new HistoryAdapter(this, itemList);
            mListView.setAdapter(adapter);
        }

    }
}
