package com.tambapps.p2p.peer_transfer.android.service.event;

public interface SendingEventHandler extends TaskEventHandler {

    void onServiceStarted();

    void onServiceTimeout();

}
