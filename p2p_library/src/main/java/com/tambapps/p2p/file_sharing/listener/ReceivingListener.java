package com.tambapps.p2p.file_sharing.listener;

import java.io.File;

public interface ReceivingListener extends TransferListener {

  void onEnd(File file);

}
