package com.tambapps.p2p.peer_transfer.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tambapps.p2p.peer_transfer.android.SendActivity;

public class SendingStartedBroadcastReceiver extends BroadcastReceiver {

    public static final String SENDING_STARTED = SendingStartedBroadcastReceiver.class.getName()
            + ".sending.started";
    private final SendActivity sendActivity;

    public SendingStartedBroadcastReceiver(SendActivity sendActivity) {
        this.sendActivity = sendActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(SENDING_STARTED)) {
            sendActivity.onSendingStarted();
        }
    }
}