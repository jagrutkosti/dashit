package dashit.uni.com.dashit.view.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import dashit.uni.com.dashit.R;
import dashit.uni.com.dashit.helper.SHAHashTasks;

/**
 * Activity that shows the visualization related to the History files.
 * Opens when the user clicks on any List Item in HistoryVerifyActivity
 */
public class VerifyFilesActivity extends AppCompatActivity {
    private TextView directoryName;
    private TextView fileViewName1;
    private TextView fileViewName2;
    private TextView fileViewName3;
    private TextView hashValueView;
    private ImageView plus;
    private ImageView downArrow;
    private ImageView bitcoin;
    private TextView hashSubmissionView;
    private ImageView txArrow;
    private TextView txHeading;
    private Button copyTxHash;
    private TextView txText;
    private RelativeLayout txLayout;
    private AppCompatButton verifyButton;

    private String savedHash;
    private String txHash;
    private String accidentLocation;
    private boolean hashCorrect;
    /**
     * Based on various category, which visualization to show
     * @param savedInstanceState super
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_files);

        directoryName = (TextView) findViewById(R.id.directoryVerify);
        fileViewName1 = (TextView) findViewById(R.id.fileName1);
        fileViewName2 = (TextView) findViewById(R.id.fileName2);
        fileViewName3 = (TextView) findViewById(R.id.fileName3);
        hashValueView = (TextView) findViewById(R.id.hashValue);
        plus = (ImageView) findViewById(R.id.plus2);
        plus.setVisibility(View.INVISIBLE);
        fileViewName3.setVisibility(View.INVISIBLE);

        downArrow = (ImageView) findViewById(R.id.arrowDown);
        bitcoin = (ImageView) findViewById(R.id.bitcoin);
        hashSubmissionView = (TextView) findViewById(R.id.hashSubmission);
        txArrow = (ImageView) findViewById(R.id.txArrow);
        txHeading = (TextView) findViewById(R.id.txHashHeading);
        txText = (TextView) findViewById(R.id.txHashText);
        txLayout = (RelativeLayout) findViewById(R.id.relativeLayoutForTx);
        copyTxHash = (Button) findViewById(R.id.button);
        verifyButton = (AppCompatButton) findViewById(R.id.verifyButton);

        Bundle extras = this.getIntent().getExtras();

        //Copy the hash to check on originstamp server
        copyTxHash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("txHash", txText.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(VerifyFilesActivity.this, R.string.instruction_transactionHash_copied, Toast.LENGTH_LONG).show();
            }
        });

        //Redirect to BlockchainInfo for details about the transaction hash
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(getString(R.string.verifyHashUrl));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        //Start new intent to view videos
        final Uri firstFileUri = Uri.fromFile(new File(extras.getString("file0")));
        fileViewName1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startVideoIntent = new Intent(Intent.ACTION_VIEW);
                startVideoIntent.setDataAndType(firstFileUri, "video/*");
                startActivity(startVideoIntent);
            }
        });

        final Uri secondFileUri = Uri.fromFile(new File(extras.getString("file1")));
        fileViewName2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startVideoIntent = new Intent(Intent.ACTION_VIEW);
                startVideoIntent.setDataAndType(secondFileUri, "video/*");
                startActivity(startVideoIntent);
            }
        });
        
        ArrayList<String> fileNames = new ArrayList<>();
        String dName = extras.getString("directory");
        directoryName.setText(dName.substring(dName.lastIndexOf("/") + 1));

        String fileName1 = extras.getString("file0");
        fileNames.add(fileName1);
        fileName1 = fileName1.substring(fileName1.lastIndexOf("/") + 1);
        fileViewName1.setText(fileName1);


        String fileName2 = extras.getString("file1");
        fileNames.add(fileName2);
        fileName2 = fileName2.substring(fileName2.lastIndexOf("/") + 1);
        fileViewName2.setText(fileName2);

        String fileName3 = extras.getString("file2");
        if(fileName3 != null && fileName3.length() > 0){
            final Uri thirdFileUri = Uri.fromFile(new File(fileName3));
            fileViewName3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent startVideoIntent = new Intent(Intent.ACTION_VIEW);
                    startVideoIntent.setDataAndType(thirdFileUri, "video/*");
                    startActivity(startVideoIntent);
                }
            });

            fileNames.add(fileName3);
            fileName3 = fileName3.substring(fileName3.lastIndexOf("/") + 1);
            fileViewName3.setText(fileName3);
            fileViewName3.setVisibility(View.VISIBLE);
            plus.setVisibility(View.VISIBLE);
        }

        accidentLocation = extras.getString("accidentLocation");
        savedHash = extras.getString("savedHash");
        txHash = extras.getString("tx_hash");
        new CheckHashCorrect().execute(dName);
    }

    /**
     * Asynchronously check ff the saved hash has not be tampered with.
     * Accordingly, show the visualization items.
     */
    public class CheckHashCorrect extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            String calculatedHash = SHAHashTasks.generateHashFromFilesAndLocation(strings[0], accidentLocation);
            if(calculatedHash.equals(savedHash))
                hashCorrect = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(hashCorrect){
                hashValueView.setText(savedHash);
                downArrow.setVisibility(View.VISIBLE);
                bitcoin.setVisibility(View.VISIBLE);
                hashSubmissionView.setVisibility(View.VISIBLE);
                if(txHash != null && txHash.length() > 0){
                    hashSubmissionView.setText(R.string.activity_verify_files_hashSubmission);
                    txLayout.setVisibility(View.VISIBLE);
                    txArrow.setVisibility(View.VISIBLE);
                    txHeading.setVisibility(View.VISIBLE);
                    txText.setText(txHash);
                    verifyButton.setVisibility(View.VISIBLE);
                }else{
                    hashSubmissionView.setText(R.string.activity_verify_files_hashNoSubmission);
                    hashSubmissionView.setTextColor(Color.RED);
                }
            }else{
                hashValueView.setText(R.string.activity_verify_files_hashNotCorrect);
                hashValueView.setTextColor(Color.RED);
            }
        }
    }
}
