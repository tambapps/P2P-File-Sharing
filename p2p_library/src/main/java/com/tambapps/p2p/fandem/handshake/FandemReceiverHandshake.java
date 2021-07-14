package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.speer.exception.HandshakeFailException;
import com.tambapps.p2p.speer.handshake.SerializedHandshake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FandemReceiverHandshake extends SerializedHandshake<ReceiverHandshakeData, SenderHandshakeData> {
  public FandemReceiverHandshake(ReceiverHandshakeData data) {
    super(Fandem.serializer(), Fandem.deserializer(SenderHandshakeData.class), data);
  }

  @Override
  public Object apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    outputStream.writeUTF("FANDEM");
    outputStream.writeUTF(Fandem.VERSION);
    if (!inputStream.readUTF().equals("FANDEM")) {
      throw new HandshakeFailException("Remote peer is not a fandem peer");
    }
    String version = inputStream.readUTF();
    Fandem.checkVersionCompatibility(version);

    serializer.serialize(data, outputStream);
    SenderHandshakeData senderHandshakeData = deserializer.deserialize(inputStream);
    validate(senderHandshakeData);
    return senderHandshakeData;
  }

  @Override
  protected void validate(SenderHandshakeData senderHandshakeData) throws HandshakeFailException {
    if (senderHandshakeData.getFileName() == null) {
      throw new HandshakeFailException("Sender should have sent file_name");
    }
    if (senderHandshakeData.getFileSize() == null) {
      throw new HandshakeFailException("Sender should have sent file_size");
    }
    if (data.getSendChecksum() && !senderHandshakeData.getChecksum().isPresent()) {
      throw new HandshakeFailException("Sender should have sent checksum");
    }
  }
}
