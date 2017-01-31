package net.johnbrooks.remindu.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import net.johnbrooks.remindu.activities.LoginActivity;
import net.johnbrooks.remindu.schedulers.BackgroundServiceScheduler;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 12/19/2016.
 */

public class PullService extends Service
{
    public PullService()
    {
        super();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d("INFO", "Background service created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        Log.d("INFO", "Background service started.");
        if (!LoginActivity.AttemptLoadSavedProfile(this))
        {
            Log.d("WARNING", "Could not load saved profile from file.");
            return;
        }

        BackgroundServiceScheduler.Initialize(this);
    }
    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

}
