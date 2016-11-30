package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.LoginActivity;
import net.johnbrooks.remindu.UserAreaActivity;
import net.johnbrooks.remindu.requests.LoginRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 11/24/2016.
 */

public class UserProfile implements Parcelable
{
    public static UserProfile PROFILE = null;

    private int UserID;
    private int Active;

    private String FullName;
    private String Username;
    private String Email;
    private String Password;

    private int PointsRemaining;
    private int PointsSent;
    private int PointsReceived;

    private List<ContactProfile> Contacts;
    private List<Reminder> Reminders;

    public UserProfile(final int id, final int active, final String fullName, final String username, final String email, final String password, final Integer pointsRemaining, final Integer pointsReceived, final Integer pointsSent)
    {
        UserID = id;
        Active = active;
        FullName = fullName;
        Username = username;
        Email = email;
        Password = password;
        PointsReceived = pointsReceived;
        PointsRemaining = pointsRemaining;
        PointsSent = pointsSent;

        Reminders = new ArrayList<>();
        Contacts = new ArrayList<>();

        Reminder test = new Reminder("This is a test reminder.");
        Reminder test2 = new Reminder("Make sure to take out the trash. ");
        Reminder test3 = new Reminder("This is a test reminder.");
        test2.SetImportant(true);

        Reminders.add(test);
        Reminders.add(test2);
        Reminders.add(test3);
    }

    public final int IsActive() { return Active; }
    public final int GetUserID() { return UserID; }
    public final String GetFullName() { return FullName; }
    public final String GetUsername() { return Username; }
    public final String GetEmail() { return Email; }
    public final String GetPassword() { return Password; }
    public final int GetPointsRemaining() { return PointsRemaining; }
    public final int GetPointsSent() { return PointsSent; }
    public final int GetPointsReceived() { return PointsReceived; }

    public List<Reminder> GetReminders() { return Reminders; }
    public List<ContactProfile> GetContacts() { return Contacts; }

    protected UserProfile(Parcel in)
    {
        Active = in.readInt();
        FullName = in.readString();
        Username = in.readString();
        Email = in.readString();
        Password = in.readString();
        PointsRemaining = in.readInt();
        PointsSent = in.readInt();
        PointsReceived = in.readInt();
    }

    public void writeToLinearLayout(Activity activity, LinearLayout layout)
    {
        resetLinearLayout(layout);

        for (Reminder r : GetReminders())
        {
            layout.addView(r.CreateWidget(activity, layout));
        }
    }

    private void resetLinearLayout(LinearLayout layout)
    {
        for (Reminder r : GetReminders())
        {
            r.SetWidget(null);
        }
        layout.removeAllViews();
    }

    public void deleteReminder(Reminder r)
    {
        //TODO: Send notification to sender that it was deleted.

        Reminders.remove(r);
        writeToLinearLayout((Activity) r.GetParent().getContext(), r.GetParent());
    }

    public void addReminder(Reminder r)
    {
        //TODO: SAVE TO SERVER

        Reminders.add(r);
        writeToLinearLayout((Activity) r.GetParent().getContext(), r.GetParent());
    }

    public void AddContact(ContactProfile profile)
    {
        GetContacts().add(profile);

        //TODO: SEND REQUEST TO SERVER
    }

    public void RemoveContact(ContactProfile profile)
    {
        GetContacts().remove(profile);

        //TODO: SEND REQUEST TO SERVER
    }

    public void RemoveContact(int _id)
    {
        for (ContactProfile p : GetContacts())
            if (p.GetID() == _id)
                RemoveContact(p);
    }

    public void RemoveContact(String _email)
    {
        for (ContactProfile p : GetContacts())
            if (p.GetEmail().equalsIgnoreCase(_email))
                RemoveContact(p);
    }

    public void Pull(Activity activity)
    {
        Log.d("INFO", "Pulling profile from server...");
        Response.Listener<String> responseListener = GetPullResponseListener();

        LoginRequest request = new LoginRequest(Email, Password, responseListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }

    private Response.Listener<String> GetPullResponseListener()
    {
        Response.Listener<String> responseListener = new Response.Listener<String>()
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
                        final int id = jsonResponse.getInt("userID");
                        final int active = jsonResponse.getInt("active");

                        final String fullName = jsonResponse.getString("fullname");
                        final String email = jsonResponse.getString("email");
                        final String username = jsonResponse.getString("username");

                        final int pointsTotal = jsonResponse.getInt("pointsRemaining");
                        final int pointsGiven = jsonResponse.getInt("pointsSent");
                        final int pointsReceived = jsonResponse.getInt("pointsReceived");

                        final String contacts = jsonResponse.getString("contacts");

                        UserProfile.PROFILE = new UserProfile(id, active, fullName, username, email, Password, pointsTotal, pointsReceived, pointsGiven);
                        for (String contact : contacts.split("&"))
                        {
                            //Log.d("INFO", contact);
                            if (contact == "" || contact == " ")
                                continue;
                            String[] key = contact.split("%");
                            /*if (key.length < 4)
                            {
                                Log.d("INFO", "Contact request is still pending.");
                                continue;
                            }*/

                            //Log.d("TEST", "Key[0] = '" + key[0] + "'");
                            if (key[0].equalsIgnoreCase("0"))
                            {
                                UserProfile.PROFILE.AddContact(new ContactProfile(Integer.parseInt(key[1]), key[2]));
                            }
                            else if (key[0].equalsIgnoreCase("1"))
                            {
                                //Log.d("INFO", "HIT " + key.length);
                                if (key.length >= 6)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], key[5]));
                                else if (key.length == 5)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], ""));
                            }


                        }
                    }
                    else
                    {
                        Log.d("SEVERE", "Profile Pull error.");
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        };

        return responseListener;
    }

    public static final Creator<UserProfile> CREATOR = new Creator<UserProfile>()
    {
        @Override
        public UserProfile createFromParcel(Parcel in)
        {
            return new UserProfile(in);
        }

        @Override
        public UserProfile[] newArray(int size)
        {
            return new UserProfile[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeInt(Active);
        parcel.writeString(FullName);
        parcel.writeString(Username);
        parcel.writeString(Email);
        parcel.writeString(Password);
        parcel.writeInt(PointsRemaining);
        parcel.writeInt(PointsSent);
        parcel.writeInt(PointsReceived);
    }
}
