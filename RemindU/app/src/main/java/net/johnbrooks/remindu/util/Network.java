package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.UserAreaActivity;

/**
 * Created by John on 12/5/2016.
 */

public class Network
{
    public static final boolean IsConnected(Activity activity)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
        {
            return true;
        }
        else
        {
            if (UserAreaActivity.GetActivity() != null)
                Snackbar.make(UserAreaActivity.GetActivity().findViewById(R.id.fab), "Unable to connect to server...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            return false;
        }
    }
    public static final boolean IsConnected(Service service)
    {
        if (service == null) { return false; }
        ConnectivityManager connectivityManager = (ConnectivityManager)service.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) == null) { return false; }
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
