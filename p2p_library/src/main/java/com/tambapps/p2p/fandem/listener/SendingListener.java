package com.tambapps.p2p.fandem.listener;

import com.tambapps.p2p.fandem.Peer;

public interface SendingListener extends TransferListener {

  /**
   * called when the sender started the sending service
   * @param self the self peer
   * @param fileName the name of the file that will be sent
   */
  void onStart(Peer self, String fileName);

}