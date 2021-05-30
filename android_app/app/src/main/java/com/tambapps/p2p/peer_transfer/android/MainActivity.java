package com.tambapps.p2p.peer_transfer.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity implements OnInitializationCompleteListener {
    public static final String RETURN_TEXT_KEY = "rtk";
    private static final int START_FILE_SERVICE = 888;

    private Snackbar snackbar;
    private InterstitialAd mInterstitialAd;
    private boolean backPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, this);
        loadInterstitiel();
        setVersionText();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (!preferences.contains("first_time")) {
            startActivity(new Intent(this, OnBoardingActivity.class));
            SharedPreferences.Editor edit = preferences.edit();
            edit.putInt("first_time", 1);
            edit.apply();
        }
    }

    private void setVersionText() {
        TextView versionText = findViewById(R.id.versionText);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText(("Fandem " + versionName));
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setVisibility(View.GONE);
        }
    }

    private void loadInterstitiel() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, BuildConfig.ACTION_FINISHED_INTERSTITIEL_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        Log.d("TAG", "The ad was dismissed.");
                        if (backPressed) {
                            finish();
                        } else {
                            loadInterstitiel();
                        }
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when fullscreen content failed to show.
                        Log.d("TAG", "The ad failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        mInterstitialAd = null;
                        Log.d("TAG", "The ad was shown.");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i("MainActivity", loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
    }

    public void sendIntent(View view) {
        startActivityForResult(new Intent(this, SendActivity.class), START_FILE_SERVICE);
    }

    public void helpIntent(View v) {
        startActivity(new Intent(this, HelpActivity.class));
    }

    public void receiveIntent(View view) {
        startActivityForResult(new Intent(this, ReceiveActivity.class), START_FILE_SERVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_FILE_SERVICE) {
            if (resultCode == RESULT_OK) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MainActivity.this);
                }
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
    public void onInitializationComplete(InitializationStatus initializationStatus) {
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd != null) {
            backPressed = true;
            mInterstitialAd.show(MainActivity.this);
        } else {
            super.onBackPressed();
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
