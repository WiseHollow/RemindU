package net.johnbrooks.remindu.requests;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class DeleteContactRequest extends StringRequest
{
    private static final String LOGIN_REQUEST_URL = "http://johnbrooks.net/remindu/scripts/removeContact.php";
    //private static final String LOGIN_REQUEST_URL = "http://127.0.0.1/remindu/scripts/removeContact.php";
    private Map<String, String> params;

    public DeleteContactRequest(String email, String password, String targetID, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Request.Method.POST, LOGIN_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        params.put("target_id", targetID);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }
}
