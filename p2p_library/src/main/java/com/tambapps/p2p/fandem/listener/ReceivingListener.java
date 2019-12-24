package com.tambapps.p2p.fandem.listener;

import java.io.File;

public interface ReceivingListener extends TransferListener {

  void onEnd(File file);

}