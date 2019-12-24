package com.tambapps.p2p.peer_transfer.android;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity {
    private final static int WRITE_PERMISSION_REQUEST = 2;
    public static final String RESUME_KEY = "resumeK",
    RETURN_TEXT_KEY = "rtk";
    private static final int START_FILE_SERVICE = 888;

    private FirebaseAnalytics analytics;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        analytics = FirebaseAnalytics.getInstance(this);
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
                Toast.makeText(this, this.getString(R.string.permissions_not_granted), Toast.LENGTH_SHORT).show();
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
                .setTitle(this.getString(R.string.grant_permission))
                .setNeutralButton(this.getString(R.string.no), null)
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
        logScreenEvent("SEND");
        startActivityForResult(new Intent(this, SendActivity.class), START_FILE_SERVICE);
    }

    public void helpIntent(View v) {
        startActivity(new Intent(this, HelpActivity.class));
    }

    private void logScreenEvent(String name) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "SCREEN");
        bundle.putString(FirebaseAnalytics.Param.CONTENT, name);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

    }
    public void receiveIntent(View view) {
        if (!hasWritePermission()) {
            writePermissionDialog();
            return;
        }
        logScreenEvent("RECEIVE");
        startActivityForResult(new Intent(this, ReceiveActivity.class), START_FILE_SERVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_FILE_SERVICE) {
            if (resultCode == RESULT_OK) {
                snackbar = Snackbar.make(findViewById(R.id.root), data.getStringExtra(RETURN_TEXT_KEY)
                        , Snackbar.LENGTH_INDEFINITE)
                        .setAction("ok", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                snackbar.show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStop() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
        super.onStop();
    }
}
