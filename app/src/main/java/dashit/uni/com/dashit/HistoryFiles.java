package dashit.uni.com.dashit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jagrut on 17-Feb-16.
 */
public class HistoryFiles {
    private List<String> filesInDirectory;
    private String directory;
    private String txHash;
    private String recipient;
    private String submissionTime;
    private String privateKey;
    private String publicKey;
    private String seed;
    private String savedHash;

    public String getSavedHash() {
        return savedHash;
    }

    public void setSavedHash(String savedHash) {
        this.savedHash = savedHash;
    }

    private boolean checkedAtServer;

    public boolean isCheckedAtServer() {
        return checkedAtServer;
    }

    public void setCheckedAtServer(boolean checkedAtServer) {
        this.checkedAtServer = checkedAtServer;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

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
