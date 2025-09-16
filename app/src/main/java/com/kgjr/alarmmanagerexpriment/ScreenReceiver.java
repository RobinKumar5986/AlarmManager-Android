package com.kgjr.alarmmanagerexpriment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            Log.d(TAG, "Screen turned ON!");
            Toast.makeText(context, "Screen ON", Toast.LENGTH_SHORT).show();
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            Log.d(TAG, "Screen turned OFF!");
            Toast.makeText(context, "Screen OFF", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Unexpected action: " + action);
            Toast.makeText(context, "Unexpected action: " + action, Toast.LENGTH_SHORT).show();
        }
    }
}