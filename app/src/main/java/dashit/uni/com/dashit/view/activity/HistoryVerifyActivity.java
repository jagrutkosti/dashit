package dashit.uni.com.dashit.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dashit.uni.com.dashit.R;
import dashit.uni.com.dashit.model.HistoryFiles;
import dashit.uni.com.dashit.view.adapter.HistoryAdapter;

/**
 * Created by Jagrut on 29-Nov-16.
 * The Activity which is shown after the user taps on 'History' from Menu item.
 * It contains a list view of directories that have recorded collisions in the past.
 * The directory includes the video files, hash and if available, data from the server.
 */

public class HistoryVerifyActivity extends AppCompatActivity {
    private ListView mListView;
    private ArrayList<HistoryFiles> itemList = new ArrayList<>();
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_verify);

        mListView = (ListView) findViewById(R.id.dates);
        populateView();
        registerForContextMenu(mListView);

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
                verifyActivity.putExtra("accidentLocation", file.getAccidentLocation());
                startActivity(verifyActivity);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        if (view.getId()== R.id.dates) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_context, contextMenu);
        }
    }

    /**
     * If the user selects delete from context menu, then delete the directory.
     * @param item The selected item in the list view
     * @return {boolean} if any action was performed or not
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.contextDelete:
                HistoryFiles directory = (HistoryFiles) mListView.getItemAtPosition(info.position);
                File directoryToDelete = new File(directory.getDirectory());
                try {
                    FileUtils.deleteDirectory(directoryToDelete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                itemList.remove(directory);
                mListView.invalidateViews();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    /**
     * Populating the list view with directory names using the HistoryAdapter as Data source
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
                files.setDirectory(dir.getAbsolutePath());

                List<String> fileNames = new ArrayList<>();
                if(dir.isDirectory()){
                    File[] filesInDir = dir.listFiles();
                    Arrays.sort(filesInDir);
                    for(File f : filesInDir){
                        fileNames.add(f.getAbsolutePath());

                        if(f.getAbsolutePath().endsWith("location.txt")){
                            File locationFile = new File(f.getAbsolutePath());
                            StringBuilder location = new StringBuilder();
                            try{
                                BufferedReader br = new BufferedReader(new FileReader(locationFile));
                                String line;
                                while((line = br.readLine()) != null){
                                    location.append(line);
                                }
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            files.setAccidentLocation(location.toString());
                        }
                    }
                }
                files.setFilesInDirectory(fileNames);
                itemList.add(files);
            }
            adapter = new HistoryAdapter(this, itemList);
            mListView.setAdapter(adapter);
        }
    }
}
