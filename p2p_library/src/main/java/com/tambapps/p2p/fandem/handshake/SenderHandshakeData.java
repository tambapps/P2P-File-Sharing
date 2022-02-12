package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.fandem.model.FileData;
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

}
