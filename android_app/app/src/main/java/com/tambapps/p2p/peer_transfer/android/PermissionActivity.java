package com.tambapps.p2p.peer_transfer.android;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionActivity extends AppCompatActivity {

  public static final int PERMISSION_REQUEST_CODE = 8;

  public boolean hasPermission(String permission) {
    return ContextCompat.checkSelfPermission(this, permission)
        == PackageManager.PERMISSION_GRANTED;
  }
  public void requestWritePermission(String permission) {
    ActivityCompat.requestPermissions(this,
        new String[]{permission},
        PERMISSION_REQUEST_CODE);
  }

  public void requestPermissionDialog(int title, int message, final String permission) {
    requestPermissionDialog(title, message, (dialogInterface, i) -> requestWritePermission(permission));
  }


  public void requestPermissionDialog(int title, int message, DialogInterface.OnClickListener onOkClickListener) {
    new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(this.getString(R.string.no), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), getString(R.string.permissions_not_granted), Toast.LENGTH_SHORT).show();
                finish();
              }
            })
            .setPositiveButton("ok", onOkClickListener)
            .setCancelable(false)
            .create()
            .show();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    if (requestCode == PERMISSION_REQUEST_CODE) {
      if (grantResults.length > 0
          && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(getApplicationContext(), this.getString(R.string.permissions_not_granted), Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }

}
