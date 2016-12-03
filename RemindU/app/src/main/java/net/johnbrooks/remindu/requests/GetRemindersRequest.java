package net.johnbrooks.remindu.requests;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import net.johnbrooks.remindu.util.UserProfile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class GetRemindersRequest extends StringRequest
{
    private static final String LOGIN_REQUEST_URL = "http://johnbrooks.net/remindu/scripts/getReminders.php";
    private Map<String, String> params;

    public GetRemindersRequest(Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, LOGIN_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id", String.valueOf(UserProfile.PROFILE.GetUserID()));
        params.put("password", UserProfile.PROFILE.GetPassword());
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }
}
