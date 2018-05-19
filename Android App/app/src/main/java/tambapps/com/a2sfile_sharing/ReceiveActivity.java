package tambapps.com.a2sfile_sharing;

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

import tambapps.com.a2sfile_sharing.service.FileReceivingJobService;

public class ReceiveActivity extends AppCompatActivity {

    private TextInputLayout portInput;
    private LinearLayout ipInput;
    private TextView ipError;

    private String downloadPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        portInput = findViewById(R.id.port_input);
        ipInput = findViewById(R.id.ip_input);
        ipError = findViewById(R.id.ip_error);
        downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        int underlineColor = getResources().getColor(R.color.colorAccent);
        final EditText portEditText = portInput.getEditText();
        portEditText.getBackground().setColorFilter(underlineColor, PorterDuff.Mode.SRC_IN);
        for (int i = 0; i < 8; i+=2) {
            final EditText editText = (EditText) ipInput.getChildAt(i);
            editText.getBackground().setColorFilter(underlineColor, PorterDuff.Mode.SRC_IN);
            final View nextFocus = i < 6 ? ipInput.getChildAt(i+2) : portInput;
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String text = s.toString();
                    if (!text.isEmpty() && editText.getError() != null) {
                        editText.setError(null);
                    }

                    if (text.length()>= 3 ||
                            text.length()>=2 && text.charAt(0) > '2') {
                        editText.clearFocus();
                        nextFocus.requestFocus();
                        if (nextFocus instanceof EditText) {
                            ((EditText) nextFocus).setCursorVisible(true);
                        }
                    }
                }
            });
        }

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

        boolean hasIpError = false;
        String[] ipFields = new String[4];
        for (int i = 0; i < 4; i++) {
            EditText editText = (EditText) ipInput.getChildAt(i * 2);
            ipFields[i] = editText.getText().toString();
            if (ipFields[i].isEmpty() || Integer.parseInt(ipFields[i])>255) {
                hasIpError = true;
            }
        }

        setIpError(hasIpError);

        if (portInput.getError() == null && !hasIpError) {
            /*
            Intent serviceIntent = new Intent(this, ReceiveFileService.class);
            serviceIntent.putExtra("id", 0);
            serviceIntent.putExtra("uploadPath", downloadPath);
            serviceIntent.putExtra("ip", getIp(ipFields));
            serviceIntent.putExtra("port", Integer.parseInt(port));
            startService(serviceIntent);
            Toast.makeText(getApplicationContext(), "Started service", Toast.LENGTH_SHORT).show();
*/
            JobScheduler jobScheduler =
                    (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

            PersistableBundle bundle = new PersistableBundle();

            bundle.putString("downloadPath", downloadPath);
            bundle.putString("peer", getIp(ipFields)+ ":" + port);

            JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(1,
                    new ComponentName(this, FileReceivingJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setExtras(bundle);

            jobScheduler.schedule(jobInfoBuilder.build());
            Intent returnIntent = new Intent();
            returnIntent.putExtra(MainActivity.RETURN_TEXT_KEY, "Service started. You can see the progress in the notification bar");
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    private String getIp(String[] ipFields) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            builder.append(ipFields[i])
                    .append(".");
        }
        return builder.append(ipFields[3])
                .toString();
    }

    private void setIpError(boolean on) {
        ipError.animate()
                .alpha(on ? 1 : 0)
                .setDuration(800)
                .start();
    }
}
