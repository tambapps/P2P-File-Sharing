package tambapps.com.a2sfile_sharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public class DialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        new AlertDialog.Builder(getApplicationContext())
                .setTitle(intent.getStringExtra("title"))
                .setMessage(intent.getStringExtra("message"))
                .setCancelable(false)
                .setNeutralButton("ok", null)
                .create()
                .show();
        finish();
    }
}
