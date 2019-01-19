package com.tambapps.p2p.peer_transfer.android;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tambapps.p2p.peer_transfer.android.analytics.Constants;
import com.tambapps.p2p.peer_transfer.android.service.FileReceivingJobService;

public class ReceiveActivity extends AppCompatActivity {

    private static final int RECEIVING_JOB_ID = 1;
    private TextInputLayout portInput;
    private IpInputHandler ipInputHandler;
    private FirebaseAnalytics analytics;

    private String downloadPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        analytics = FirebaseAnalytics.getInstance(this);

        portInput = findViewById(R.id.port_input);
        downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        int underlineColor = getResources().getColor(R.color.colorAccent);

        LinearLayout ipInputLayout = findViewById(R.id.ip_input);
        TextView ipError = findViewById(R.id.ip_error);
        ipInputHandler = new IpInputHandler(ipInputLayout, ipError,
                underlineColor);

        EditText portEditText = portInput.getEditText();
        portEditText.getBackground().setColorFilter(underlineColor, PorterDuff.Mode.SRC_IN);
        portEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    portInput.setError(null);
                }
            }
        });
    }

    public void startReceiving(View view) {
        String port = portInput.getEditText().getText().toString();
        if (port.isEmpty()) {
            portInput.setError("You must enter the port");
        }

        boolean hasIpError = ipInputHandler.hasIpError();
        ipInputHandler.setIpError(hasIpError);

        if (portInput.getError() == null && !hasIpError) {
            JobScheduler jobScheduler =
                    (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

            PersistableBundle bundle = new PersistableBundle();

            bundle.putString("downloadPath", downloadPath);
            bundle.putString("peer", ipInputHandler.getIp() + ":" + port);
            bundle.putInt("id", RECEIVING_JOB_ID);

            JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(1,
                    new ComponentName(this, FileReceivingJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setExtras(bundle);

            logReceive();
            jobScheduler.schedule(jobInfoBuilder.build());
            Intent returnIntent = new Intent();
            returnIntent.putExtra(MainActivity.RETURN_TEXT_KEY, "Service started. You can see the progress in the notification bar");
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    private void logReceive() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Value.SERVICE_START);
        bundle.putString(FirebaseAnalytics.Param.METHOD, "RECEIVE");
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private class IpInputHandler {

        private int[] ipFields = new int[] {-1, -1, -1, -1};
        private TextView ipError;

        IpInputHandler(LinearLayout ipInputLayout, TextView ipError, int underlineColor) {
            this.ipError = ipError;

            for (int i = 0; i < 8; i+=2) {
                final int position = i/2;
                final EditText editText = (EditText) ipInputLayout.getChildAt(i);
                editText.getBackground().setColorFilter(underlineColor, PorterDuff.Mode.SRC_IN);
                final View nextFocus = i < 6 ? ipInputLayout.getChildAt(i + 2) : portInput;
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String text  = s.toString();
                        if (text.isEmpty()) {
                            ipFields[position] = -1;
                        } else {
                            ipFields[position] = Integer.parseInt(text);
                        }

                        if (ipFields[position] >= 30) {
                            editText.clearFocus();
                            nextFocus.requestFocus();
                            if (nextFocus instanceof EditText) {
                                ((EditText) nextFocus).setCursorVisible(true);
                            }
                        }

                        if (!hasIpError() && errorVisible()) {
                            setIpError(false);
                        }
                    }
                });
            }
        }

        private boolean hasIpError() {
            for (int i = 0; i < 4; i++) {
                if (ipFields[i] > 255 || ipFields[i] < 0) {
                    return true;
                }
            }
            return false;
        }

        void setIpError(boolean on) {
            ipError.animate()
                    .alpha(on ? 1 : 0)
                    .setDuration(400)
                    .start();
        }

        boolean errorVisible() {
            return ipError.getAlpha() == 1f;
        }
        private String getIp() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                builder.append(ipFields[i])
                        .append(".");
            }
            return builder.append(ipFields[3])
                    .toString();
        }
    }
}
