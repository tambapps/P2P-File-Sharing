package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.speer.exception.HandshakeFailException;
import com.tambapps.p2p.speer.handshake.SerializedHandshake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FandemSenderHandshake extends SerializedHandshake<SenderHandshakeData, ReceiverHandshakeData> {

  public FandemSenderHandshake(SenderHandshakeData data) {
    super(Fandem.serializer(), Fandem.deserializer(ReceiverHandshakeData.class), data);
  }

  @Override
  public ReceiverHandshakeData apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    outputStream.writeUTF("FANDEM");
    outputStream.writeUTF(Fandem.VERSION);
    if (!inputStream.readUTF().equals("FANDEM")) {
      throw new HandshakeFailException("Remote peer is not a fandem peer");
    }
    String version = inputStream.readUTF();
    Fandem.checkVersionCompatibility(version);

    ReceiverHandshakeData receiverData = deserializer.deserialize(inputStream);
    validate(receiverData);

    serializer.serialize(data, outputStream);
    return receiverData;
  }

  @Override
  protected void validate(ReceiverHandshakeData receiverHandshakeData) throws HandshakeFailException {

  }

}
