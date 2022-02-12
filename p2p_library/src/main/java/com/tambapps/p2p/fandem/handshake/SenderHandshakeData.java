package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.fandem.model.FileData;
import com.tambapps.p2p.speer.exception.HandshakeFailException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SenderHandshakeData {

  List<? extends FileData> files;

  public SenderHandshakeData(List<? extends FileData> files) {
    this.files = files;
  }

  public void validate() throws HandshakeFailException {
    if (files == null || files.isEmpty()) {
      throw new HandshakeFailException("Sender should have sent files");
    }
    for (FileData fileData : files) {
      if (fileData.getFileName() == null) {
        throw new HandshakeFailException("Sender should have sent file_name");
      }
      if (fileData.getFileSize() == 0L) {
        throw new HandshakeFailException("Sender should have sent file_size");
      }
    }
  }
}
