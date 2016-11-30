package dashit.uni.com.dashit;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class VerifyFilesActivity extends AppCompatActivity {
    private TextView directoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_files);

        directoryName = (TextView) findViewById(R.id.directoryVerify);
        String name = this.getIntent().getExtras().getString("directory");
        directoryName.setText(name);
    }

}
