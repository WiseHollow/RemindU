package net.johnbrooks.remindu.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.schedulers.MasterScheduler;

/**
 * Created by John on 12/5/2016.
 */

public class Network
{
    private static RequestQueue REQUEST_QUEUE = null;
    public static boolean PushRequest(StringRequest request)
    {
        if (REQUEST_QUEUE == null)
            return false;

        REQUEST_QUEUE.add(request);
        return true;
    }

    public static boolean InitializeNetworkQueue(ContextWrapper contextWrapper)
    {
        if (contextWrapper == null)
            return false;

        if (REQUEST_QUEUE != null)
            return true;

        REQUEST_QUEUE = Volley.newRequestQueue(contextWrapper);
        boolean result = REQUEST_QUEUE != null;

        Log.d("NETWORK", "REQUEST_QUEUE initialization: " + result);

        return result;
    }

    public static final boolean IsConnected()
    {
        ContextWrapper context = MasterScheduler.GetInstance().GetContextWrapper();
        if (context == null)
            return false;

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (connectivityManager.getNetworkInfo(connectivityManager.getActiveNetwork()).getState() == NetworkInfo.State.CONNECTED)
                connected = true;
        }
        else if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
        {
            connected = true;
        }
        //if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {

        }



        if (connected)
            return true;
        else
        {
            if (UserAreaActivity.GetActivity() != null)
            {
                View focus = UserAreaActivity.GetActivity().findViewById(R.id.fab);
                if (focus != null)
                {
                    Snackbar.make(focus, "Unable to connect to server...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

            return false;
        }
    }

    /*public static final boolean IsConnected(Activity activity)
    {
        if (UserProfile.PROFILE != null && UserProfile.PROFILE.AccountIsDisabled())
            return false;
        ConnectivityManager connectivityManager = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
        {
            return true;
        }
        else
        {
            if (UserAreaActivity.GetActivity() != null)
            {
                View focus = UserAreaActivity.GetActivity().findViewById(R.id.fab);
                if (focus != null)
                {
                    Snackbar.make(focus, "Unable to connect to server...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }


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
    }*/
}
