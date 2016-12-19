package net.johnbrooks.remindu.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by John on 12/19/2016.
 */

public class MyReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent serviceIntent = new Intent(context, PullService.class);
        context.startService(serviceIntent);
    }
}
