package net.johnbrooks.remindu.requests;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class DeleteReminderRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/deleteReminder.php";
    private Map<String, String> params;

    public DeleteReminderRequest(int user_id, String password, int reminderID, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id", String.valueOf(user_id));
        params.put("password", password);
        params.put("reminder_id", String.valueOf(reminderID));
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }
}
