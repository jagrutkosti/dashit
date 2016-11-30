package dashit.uni.com.dashit;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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

        copyTxHash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("txHash", txText.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(VerifyFilesActivity.this, "Transaction Hash Copied", Toast.LENGTH_LONG).show();
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://blockchain.info/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        ArrayList<String> fileNames = new ArrayList<>();
        String name = this.getIntent().getExtras().getString("directory");
        directoryName.setText(name);

        String fileName1 = this.getIntent().getExtras().getString("file0");
        fileNames.add(fileName1);
        fileName1 = fileName1.substring(fileName1.lastIndexOf("/") + 1);
        fileViewName1.setText(fileName1);

        String fileName2 = this.getIntent().getExtras().getString("file1");
        fileNames.add(fileName2);
        fileName2 = fileName2.substring(fileName2.lastIndexOf("/") + 1);
        fileViewName2.setText(fileName2);

        String fileName3 = this.getIntent().getExtras().getString("file2");
        if(fileName3 != null && fileName3.length() > 0){
            fileNames.add(fileName3);
            fileName3 = fileName3.substring(fileName3.lastIndexOf("/") + 1);
            fileViewName3.setText(fileName3);
            fileViewName3.setVisibility(View.VISIBLE);
            plus.setVisibility(View.VISIBLE);
        }

        String savedHash = this.getIntent().getExtras().getString("savedHash");
        boolean hashCorrect = verifyHash(fileNames, savedHash);

        String txHash = this.getIntent().getExtras().getString("tx_hash");
        if(hashCorrect){
            hashValueView.setText(savedHash);
            downArrow.setVisibility(View.VISIBLE);
            bitcoin.setVisibility(View.VISIBLE);
            hashSubmissionView.setVisibility(View.VISIBLE);
            if(txHash != null && txHash.length() > 0){
                hashSubmissionView.setText(R.string.hashSubmission);
                txLayout.setVisibility(View.VISIBLE);
                txArrow.setVisibility(View.VISIBLE);
                txHeading.setVisibility(View.VISIBLE);
                txText.setText(txHash);
                verifyButton.setVisibility(View.VISIBLE);
            }else{
                hashSubmissionView.setText(R.string.hashNoSubmission);
                hashSubmissionView.setTextColor(Color.RED);
            }
        }else{
            hashValueView.setText(R.string.hashNotCorrect);
            hashValueView.setTextColor(Color.RED);
        }
    }

    public boolean verifyHash(ArrayList<String> fileNames, String savedHash){
        boolean hashCheck = false;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for(String filePath : fileNames){
            File file = new File(filePath);
            if(file.exists()){
                byte[] byteArray = new byte[(int) file.length()];
                try {
                    InputStream fileIS = new FileInputStream(file);
                    fileIS.read(byteArray);
                    outputStream.write(byteArray);
                    fileIS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        byte[] finalByte = outputStream.toByteArray();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(finalByte);
            byte[] mdBytes = md.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<mdBytes.length;i++) {
                hexString.append(Integer.toHexString(0xFF & mdBytes[i]));
            }
            if(hexString.toString().equalsIgnoreCase(savedHash))
                hashCheck = true;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashCheck;
    }

}
