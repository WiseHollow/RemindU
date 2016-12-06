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
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/removeContact.php";
    private Map<String, String> params;

    public DeleteContactRequest(String email, String password, String targetID, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Request.Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        params.put("target", targetID);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }
}
