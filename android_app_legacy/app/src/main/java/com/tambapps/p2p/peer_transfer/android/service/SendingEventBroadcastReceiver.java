package com.tambapps.p2p.peer_transfer.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tambapps.p2p.peer_transfer.android.SendActivity;

public class SendingEventBroadcastReceiver extends BroadcastReceiver {

    public static final String SENDING_STARTED = SendingEventBroadcastReceiver.class.getName()
            + ".sending.started";
    public static final String SERVICE_TIMEOUT = SendingEventBroadcastReceiver.class.getName()
            + ".sending.timeout";

    private final SendActivity sendActivity;

    public SendingEventBroadcastReceiver(SendActivity sendActivity) {
        this.sendActivity = sendActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(SENDING_STARTED)) {
            sendActivity.onSendingStarted();
        } else if (action.equals(SERVICE_TIMEOUT)) {
            sendActivity.onSendingCanceled();
        }
    }
}