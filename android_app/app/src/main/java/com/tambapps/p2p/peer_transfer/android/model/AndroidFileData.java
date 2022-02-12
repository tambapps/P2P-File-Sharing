package com.tambapps.p2p.peer_transfer.android.model;

import android.content.ContentResolver;
import android.net.Uri;

import com.tambapps.p2p.fandem.model.FileData;
import com.tambapps.p2p.fandem.model.SendingFileData;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class AndroidFileData extends FileData {

  // storing string to facilitate deserialization
  private String uri;

  private transient InputStream inputStream;


  public AndroidFileData(String fileName, long fileSize, Uri uri) {
    super(fileName, fileSize, null);
    this.uri = uri.toString();
  }

  public AndroidFileData(String fileName, long fileSize, String checksum, String uri) {
    super(fileName, fileSize, checksum);
    this.uri = uri;
  }

  public AndroidFileData() {
  }

  public Uri getUri() {
    return Uri.parse(uri);
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public SendingFileData toSendingFileData() {
    return new SendingFileData(getFileName(), getFileSize(), getChecksum().orElse(null), this::getInputStream);
  }

  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public InputStream getInputStream() {
    return inputStream;
  }
}
