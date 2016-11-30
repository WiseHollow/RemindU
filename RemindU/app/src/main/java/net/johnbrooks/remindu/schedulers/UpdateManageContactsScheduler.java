package net.johnbrooks.remindu.schedulers;

import android.os.Handler;
import android.widget.TextView;

import net.johnbrooks.remindu.ManageContactsActivity;
import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.UserAreaActivity;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 11/30/2016.
 */

public class UpdateManageContactsScheduler
{
    private static UpdateManageContactsScheduler scheduler = null;
    public static void Initialize(ManageContactsActivity activity)
    {
        if (scheduler == null)
            scheduler = new UpdateManageContactsScheduler(activity);
    }
    public static void Cancel()
    {
        scheduler.stopRepeatingTask();
        scheduler = null;
    }

    private ManageContactsActivity Activity = null;
    private final int mInterval = 5000; // milliseconds
    private Handler mHandler;

    public UpdateManageContactsScheduler(ManageContactsActivity activity)
    {
        Activity = activity;
        mHandler = new Handler();
        Activity = activity;
        startRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable()
    {
        @Override
        public void run() {
            try
            {
                Update();
            } finally
            {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void Update()
    {
        if (Activity == null)
        {
            Cancel();
        }
        else
        {
            Activity.UpdateContactsList();
        }
    }

    private void startRepeatingTask()
    {
        mStatusChecker.run();
    }

    public void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
