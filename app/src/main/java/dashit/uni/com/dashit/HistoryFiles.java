package dashit.uni.com.dashit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jagrut on 17-Feb-16.
 */
public class HistoryFiles {
    private List<String> filesInDirectory;
    private String directory;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public HistoryFiles(){
        filesInDirectory = new ArrayList<>();
    }

    public List<String> getFilesInDirectory() {
        return filesInDirectory;
    }

    public void setFilesInDirectory(List<String> filesInDirectory) {
        this.filesInDirectory = filesInDirectory;
    }
}
