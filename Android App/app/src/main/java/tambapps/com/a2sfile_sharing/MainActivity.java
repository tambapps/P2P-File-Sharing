package tambapps.com.a2sfile_sharing;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import tambapps.com.a2sfile_sharing.service.ReceiveFileService;
import tambapps.com.a2sfile_sharing.service.SendFileService;

public class MainActivity extends AppCompatActivity {
    private final static int WRITE_PERMISSION_REQUEST = 2;
    public static final String RESUME_KEY = "resumeK",
    RETURN_TEXT_KEY = "rtk";
    private static final int START_FILE_SERVICE = 888;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String action = getIntent().getAction();
        if (action != null) {
            if (action.endsWith(SendFileService.class.getName())) {
                Intent intent = new Intent(this, SendActivity.class);
                intent.putExtra(RESUME_KEY, true);
                startActivity(intent);
            } else if (action.endsWith(ReceiveFileService.class.getName())) {
                Intent intent = new Intent(this, ReceiveActivity.class);
                intent.putExtra(RESUME_KEY, true);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!hasWritePermission()) {
            writePermissionDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You didn't grant write permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean hasWritePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
    private void requestWritePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                WRITE_PERMISSION_REQUEST);
    }

    private void writePermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Please, grant file writing permission")
                .setNeutralButton("no", null)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestWritePermission();
                    }
                })
                .create()
                .show();
    }

    public void sendIntent(View view) {
        if (!hasWritePermission()) {
            writePermissionDialog();
            return;
        }
        startActivityForResult(new Intent(this, SendActivity.class), START_FILE_SERVICE);
    }

    public void receiveIntent(View view) {
        if (!hasWritePermission()) {
            writePermissionDialog();
            return;
        }
        startActivityForResult(new Intent(this, ReceiveActivity.class), START_FILE_SERVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_FILE_SERVICE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(findViewById(R.id.root), data.getStringExtra(RETURN_TEXT_KEY)
                        , Snackbar.LENGTH_INDEFINITE)
                        .setAction("ok", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        })
                        .show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
