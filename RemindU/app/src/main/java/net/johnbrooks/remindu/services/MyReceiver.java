package net.johnbrooks.remindu.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by John on 12/19/2016.
 */

public class MyReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean startup = sPref.getBoolean("boot_switch", true);

        if (startup == false)
        {
            Log.d("INFO", "Skipping startup service.");
            return;
        }

        Intent serviceIntent = new Intent(context, PullService.class);
        context.startService(serviceIntent);
    }
}
