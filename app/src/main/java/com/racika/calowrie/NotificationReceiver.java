package com.racika.calowrie;

import static com.racika.calowrie.SettingsActivity.SIX_HOURS_NOTIFICATION_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SettingsActivity settingsActivity = new SettingsActivity();
        settingsActivity.showNotification("Reminder: Six hours have passed", SIX_HOURS_NOTIFICATION_ID);
    }
}
