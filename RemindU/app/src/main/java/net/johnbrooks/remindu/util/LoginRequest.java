package net.johnbrooks.remindu.util;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 11/23/2016.
 */

public class LoginRequest extends StringRequest
{
    private static final String LOGIN_REQUEST_URL = "http://johnbrooks.net/remindu/scripts/login.php";
    private Map<String, String> params;

    public LoginRequest(String fullname, String username, String email, String password, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Request.Method.POST, LOGIN_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("fullname", fullname);
        params.put("username", username);
        params.put("email", email);
        params.put("password", password);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }
}
