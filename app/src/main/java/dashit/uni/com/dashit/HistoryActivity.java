package dashit.uni.com.dashit;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jagrut on 17-Feb-16.
 */
public class HistoryActivity extends AppCompatActivity {
    ExpandableListViewAdapter adapter;
    ExpandableListView expListView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        expListView = (ExpandableListView)findViewById(R.id.lvExp);
        populateView();
    }

    public void populateView(){
        File rootDirectory = new File(Environment.getExternalStorageDirectory().toString()+"/dashitHistory/");
        if(rootDirectory.exists()){
            File[] directories = new File(rootDirectory.getPath()).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            List<HistoryFiles> itemList = new ArrayList<>();
            for(int i=0;i<directories.length;i++){
                HistoryFiles directory = new HistoryFiles();
                directory.setDirectory(directories[i].getPath().substring(directories[i].getPath().lastIndexOf("/")+1));
                File fileList = new File(directories[i].getPath());
                List<String> fileNames = Arrays.asList(fileList.list());
                directory.setFilesInDirectory(fileNames);
                itemList.add(directory);
            }

            adapter = new ExpandableListViewAdapter(this,itemList);
            expListView.setAdapter(adapter);
        }

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
