package com.tambapps.p2p.fandem.handshake;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SenderHandshakeData {

  String fileName;
  Long fileSize;
  // nullable
  String checksum;

  public SenderHandshakeData(String fileName, Long fileSize) {
    this.fileName = fileName;
    this.fileSize = fileSize;
  }

  public Optional<String> getChecksum() {
    return Optional.ofNullable(checksum);
  }
}
