package net.johnbrooks.remindu.requests;

import android.app.Activity;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.ReminderListActivity;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.Network;
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

public class SendReminderRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/sendReminder.php";
    private Map<String, String> params;

    public SendReminderRequest(int user_id_from, int user_id_to, String password, String message, boolean important, Date date, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id_from", String.valueOf(user_id_from));
        params.put("user_id_to", String.valueOf(user_id_to));
        params.put("password", password);
        params.put("message", message);
        params.put("important", String.valueOf((important) ? 1 : 0));
        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        String dateString = formatter.format(date);
        params.put("date", dateString);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetSendResponseListener(final Activity activity)
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
                    String message = jsonResponse.getString("message");

                    Log.d("INFO", "Received response: " + success);

                    if (success)
                    {
                        //TODO: get reminder data from send, so we can add it locally.

                        JSONObject reminderJsonResponse = jsonResponse.getJSONObject("reminder");
                        int id = Integer.parseInt(reminderJsonResponse.getString("id"));
                        int from = Integer.parseInt(reminderJsonResponse.getString("id_from"));
                        int to = Integer.parseInt(reminderJsonResponse.getString("id_to"));
                        int important = Integer.parseInt(reminderJsonResponse.getString("important"));
                        String rMessage = reminderJsonResponse.getString("message");
                        String dateString = reminderJsonResponse.getString("date");
                        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                        Date date;
                        try
                        {
                            date = formatter.parse(dateString);
                            Reminder.LoadReminder(true, id, from, to, rMessage, (important > 0) ? true : false, date, Reminder.ReminderState.NOT_STARTED);
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }

                        Log.d("INFO", "Reminder ID: " + id);

                        MasterScheduler.GetInstance(activity).Call();
                        if (activity != null)
                            activity.finish();
                        if (ReminderListActivity.GetActivity() != null)
                            ReminderListActivity.GetActivity().RefreshReminderLayout();
                    }
                    else
                    {
                        Log.d("ERROR", "Message: " + message);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void SendRequest(final Activity activity, Reminder reminder)
    {
        if (!Network.IsConnected(activity)) { return; }

        SendReminderRequest request = new SendReminderRequest(UserProfile.PROFILE.GetUserID(), reminder.GetTo(), UserProfile.PROFILE.GetPassword(), reminder.GetMessage(), reminder.GetImportant(), reminder.GetDate(), GetSendResponseListener(activity));
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
