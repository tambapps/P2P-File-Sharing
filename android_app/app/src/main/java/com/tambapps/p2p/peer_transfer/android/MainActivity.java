package com.tambapps.p2p.peer_transfer.android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity {
    public static final String RETURN_TEXT_KEY = "rtk";
    private static final int START_FILE_SERVICE = 888;

    private FirebaseAnalytics analytics;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        analytics = FirebaseAnalytics.getInstance(this);
    }

    public void sendIntent(View view) {
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
                        })
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                View view = snackbar.getView();
                view.setBackgroundColor(getResources().getColor(R.color.gradientDown));
                TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                tv.setTextColor(Color.BLACK);
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
