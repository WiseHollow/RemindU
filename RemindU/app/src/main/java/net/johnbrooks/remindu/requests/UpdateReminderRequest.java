package net.johnbrooks.remindu.requests;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class UpdateReminderRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/updateReminder.php";
    private Map<String, String> params;

    public UpdateReminderRequest(Reminder reminder, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id", String.valueOf(UserProfile.PROFILE.GetUserID()));
        params.put("password", UserProfile.PROFILE.GetPassword());
        params.put("reminder_id", String.valueOf(reminder.GetID()));
        params.put("state", String.valueOf(reminder.GetStateOrdinal()));

        final Date now = new Date();
        final DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        final String dateString = formatter.format(now);
        params.put("date", dateString);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }
}
