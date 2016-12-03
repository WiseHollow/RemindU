package net.johnbrooks.remindu.requests;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class SendReminderRequest extends StringRequest
{
    private static final String LOGIN_REQUEST_URL = "http://johnbrooks.net/remindu/scripts/sendReminder.php";
    private Map<String, String> params;

    public SendReminderRequest(int user_id_from, int user_id_to, String password, String message, boolean important, Date date, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, LOGIN_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id_from", String.valueOf(user_id_from));
        params.put("user_id_to", String.valueOf(user_id_to));
        params.put("password", password);
        params.put("message", message);
        params.put("important", String.valueOf((important) ? 1 : 0));
        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
        String dateString = formatter.format(date);
        params.put("date", dateString);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }
}
