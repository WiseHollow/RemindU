package net.johnbrooks.remindu.requests;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class GetLatestVersionRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/getLatestVersion.php";
    private Map<String, String> params;

    public GetLatestVersionRequest(Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetResponseListener(final String currentVersion)
    {
        return new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    if (success)
                    {
                        String version = jsonResponse.getString("version");

                        if (!version.equalsIgnoreCase(currentVersion))
                        {
                            String message = jsonResponse.getString("message");
                            Log.d("INFO", "There is an update available");

                            if (UserAreaActivity.GetActivity() != null)
                            {
                                AlertDialog.Builder errorDialog = new AlertDialog.Builder(UserAreaActivity.GetActivity());
                                errorDialog.setMessage("Update is available: " + version + "\n\nVersion notice: " + message)
                                        .setNegativeButton("Okay", null)
                                        .create()
                                        .show();
                            }
                        }
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void SendRequest(final Activity activity)
    {
        PackageInfo pInfo;
        String version;

        try
        {
            pInfo = (activity == null) ? UserAreaActivity.GetActivity().getPackageManager().getPackageInfo(UserAreaActivity.GetActivity().getPackageName(), 0) : activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
            return;
        }

        Response.Listener<String> responseListener = GetResponseListener(version);
        GetLatestVersionRequest request = new GetLatestVersionRequest(responseListener);
        RequestQueue queue = (activity == null) ? Volley.newRequestQueue(UserAreaActivity.GetActivity()) : Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
