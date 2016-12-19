package net.johnbrooks.remindu.requests;

import android.app.Activity;
import android.app.Service;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class GetRemindersRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/getReminders.php";
    private Map<String, String> params;

    public GetRemindersRequest(Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id", String.valueOf(UserProfile.PROFILE.GetUserID()));
        params.put("password", UserProfile.PROFILE.GetPassword());
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetReceivedResponseListener()
    {
        return new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    for (Reminder r : UserProfile.PROFILE.GetReminders())
                        r.SetOld(true);
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    String errorMessage = jsonResponse.getString("message");

                    Log.d("INFO", "Received response: " + success);

                    if (success)
                    {
                        int size = jsonResponse.getInt("size");

                        for(int i = 0; i < size; i++)
                        {
                            int id = jsonResponse.getJSONObject(String.valueOf(i)).getInt("id");

                            String message = jsonResponse.getJSONObject(String.valueOf(i)).getString("message");
                            int state = jsonResponse.getJSONObject(String.valueOf(i)).getInt("state");
                            int important = jsonResponse.getJSONObject(String.valueOf(i)).getInt("important");
                            String dateString = jsonResponse.getJSONObject(String.valueOf(i)).getString("date");
                            DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                            Date date = formatter.parse(dateString);

                            int from = jsonResponse.getJSONObject(String.valueOf(i)).getInt("user_id_from");
                            int to = jsonResponse.getJSONObject(String.valueOf(i)).getInt("user_id_to");

                            String dateInProgress = jsonResponse.getJSONObject(String.valueOf(i)).getString("date_in_progress");
                            String dateComplete = jsonResponse.getJSONObject(String.valueOf(i)).getString("date_complete");

                            Reminder r = Reminder.LoadReminder(false, id, from, to, message, important > 0 ? true : false, date, Reminder.ReminderState.values()[state]);
                            if (!dateInProgress.equalsIgnoreCase("null"))
                                r.SetDateInProgress(dateInProgress);
                            if (!dateComplete.equalsIgnoreCase("null"))
                                r.SetDateComplete(dateComplete);
                        }
                    }
                    else
                    {
                        Log.d("ERROR", "Message: " + errorMessage);
                    }

                    for (int i = 0; i < UserProfile.PROFILE.GetReminders().size(); i++)
                    {
                        Reminder r = UserProfile.PROFILE.GetReminders().get(i);
                        if (r.IsOld() == true && r.GetID() != 0)
                        {
                            UserProfile.PROFILE.GetReminders().remove(r);
                            i--;
                        }
                    }

                    UserProfile.PROFILE.RefreshReminderLayout();
                    UserProfile.PROFILE.SaveRemindersToFile(UserAreaActivity.GetActivity());
                } catch (JSONException e)
                {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void SendRequest(final Activity activity)
    {
        Response.Listener<String> reminderResponseListener = GetReceivedResponseListener();
        GetRemindersRequest request = new GetRemindersRequest(reminderResponseListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }

    public static void SendRequest(final Service service)
    {
        if (UserProfile.PROFILE == null)
            return;

        Response.Listener<String> reminderResponseListener = GetReceivedResponseListener();
        GetRemindersRequest request = new GetRemindersRequest(reminderResponseListener);
        RequestQueue queue = Volley.newRequestQueue(service);
        queue.add(request);
    }
}
