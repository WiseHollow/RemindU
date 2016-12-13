package net.johnbrooks.remindu.schedulers;

import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 11/30/2016.
 */

public class ShowCoinGainScheduler
{
    private static ShowCoinGainScheduler scheduler = null;
    public static void Initialize()
    {
        if (scheduler == null)
            scheduler = new ShowCoinGainScheduler();
    }
    public static void Cancel()
    {
        scheduler.stopRepeatingTask();
        scheduler = null;
    }

    private ImageView view = null;
    private final int mInterval = 200; // milliseconds
    private Handler mHandler;
    private int index = 0;
    Integer[] ids = { R.drawable.coins_frame_1, R.drawable.coins_frame_2, R.drawable.coins_frame_3, R.drawable.coins_frame_4, R.drawable.coins_frame_5, R.drawable.coins_frame_6, R.drawable.coins_frame_7,
            R.drawable.coins_frame_8, R.drawable.coins_frame_9, R.drawable.coins_frame_10 };

    public ShowCoinGainScheduler()
    {
        if (UserAreaActivity.GetActivity() == null)
            return;
        view = (ImageView) UserAreaActivity.GetActivity().findViewById(R.id.coin_changes);
        mHandler = new Handler();
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
        if (scheduler == null)
        {
            //TODO: Make handler cancel the entire schedule.
            Log.d("SEVERE", "hit");
            return;
        }
        index++;
        if (UserAreaActivity.GetActivity() == null || index >= ids.length)
        {
            view.setImageResource(0);
            Cancel();
        }
        else
        {
            view.setImageResource(ids[index]);
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
