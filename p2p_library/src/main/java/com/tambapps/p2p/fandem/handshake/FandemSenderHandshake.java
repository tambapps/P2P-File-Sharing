package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.speer.exception.HandshakeFailException;
import com.tambapps.p2p.speer.handshake.SerializedHandshake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

public class FandemSenderHandshake extends SerializedHandshake<SenderHandshakeData, ReceiverHandshakeData> {

  private final Callable<String> checksumSupplier;

  public FandemSenderHandshake(SenderHandshakeData data, Callable<String> checksumSupplier) {
    super(Fandem.serializer(), Fandem.deserializer(ReceiverHandshakeData.class), data);
    this.checksumSupplier = checksumSupplier;
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
    // TODO check version incompatibility

    ReceiverHandshakeData receiverData = deserializer.deserialize(inputStream);
    validate(receiverData);

    if (receiverData.getSendChecksum()) {
      data.setChecksum(getChecksum());
    }
    serializer.serialize(data, outputStream);
    return receiverData;
  }

  private String getChecksum() throws IOException {
    try {
      return checksumSupplier.call();
    } catch (Exception e) {
      throw new IOException("Couldn't compute checksum", e);
    }
  }

  @Override
  protected void validate(ReceiverHandshakeData receiverHandshakeData) throws HandshakeFailException {
    if (receiverHandshakeData.getSendChecksum() == null) {
      throw new HandshakeFailException("Sender should have sent send_checksum");
    }
  }

}
