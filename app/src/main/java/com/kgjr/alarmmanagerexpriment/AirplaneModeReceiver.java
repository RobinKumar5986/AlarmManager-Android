package com.kgjr.alarmmanagerexpriment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AirplaneModeReceiver extends BroadcastReceiver {
    private static final String TAG = "AirplaneModeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Airplane mode changed");

    }
}