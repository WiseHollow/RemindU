package net.johnbrooks.remindu.schedulers;

import android.app.Activity;
import android.app.Service;
import android.os.Handler;
import android.util.Log;

import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 2/7/2017.
 */

public class MasterScheduler
{
    private static MasterScheduler masterScheduler;

    /**
     * @param Activity
     * @return an instance of MasterScheduler. Cannot return null.
     */
    public static final MasterScheduler GetInstance(final Activity Activity)
    {
        if (masterScheduler == null)
        {
            masterScheduler = new MasterScheduler(Activity);
        }

        return masterScheduler;
    }
    /**
     * @param Service
     * @return an instance of MasterScheduler. Cannot return null.
     */
    public static final MasterScheduler GetInstance(final Service Service)
    {
        if (masterScheduler == null)
        {
            masterScheduler = new MasterScheduler(Service);
        }

        return masterScheduler;
    }

    /**
     * @return the stored instance of MasterScheduler. May be null if called early.
     */
    public static final MasterScheduler GetInstance()
    {
        return masterScheduler;
    }

    private final int Interval = 60000;
    private Handler Handler = null;
    private Activity Activity = null;
    private Service Service = null;

    private MasterScheduler(final Activity Activity)
    {
        this.Handler = new Handler();
        this.Activity = Activity;
    }

    private MasterScheduler(final Service Service)
    {
        this.Handler = new Handler();
        this.Service = Service;
    }

    public final Activity GetActivity() { return Activity; }
    public final Service GetService() { return Service; }

    public void StartRepeatingTasks()
    {
        StopRepeatingTasks();
        ScheduleProcessor.run();
    }

    public void StopRepeatingTasks()
    {
        Handler.removeCallbacks(ScheduleProcessor);
    }

    public void Call()
    {
        Update();
    }

    private void Update()
    {
        if (GetActivity() == null && GetService() == null)
        {
            Log.d("SEVERE", "MasterScheduler: Service and Activity are null. Cancelling tasks.");
            StopRepeatingTasks();
            return;
        }

        //
        //TODO: Call all functions needed
        //

        if (UserProfile.PROFILE == null)
            return;

        UserProfile.PROFILE.Pull();
        UserProfile.PROFILE.RefreshReminderLayout();
        UserProfile.PROFILE.ProcessReminders();
    }

    Runnable ScheduleProcessor = new Runnable()
    {
        @Override
        public void run()
        {
            Update();

            if (masterScheduler != null)
                Handler.postDelayed(ScheduleProcessor, Interval);
        }
    };
}
