package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.ActivateAccountActivity;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.requests.DeleteReminderRequest;
import net.johnbrooks.remindu.requests.GetRemindersRequest;
import net.johnbrooks.remindu.requests.LoginRequest;
import net.johnbrooks.remindu.requests.UpdateReminderRequest;
import net.johnbrooks.remindu.schedulers.ShowCoinGainScheduler;

import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private int Coins;

    private List<ContactProfile> Contacts;
    private List<Reminder> Reminders;
    private List<Integer> ReminderIgnores;

    private Reminder activeReminder;

    public UserProfile(int id, final int active, final String fullName, final String username, final String email, final String password, final Integer coins)
    {
        UserID = id;
        Active = active;
        FullName = fullName;
        Username = username;
        Email = email;
        Password = password;
        Coins = coins;

        Reminders = new ArrayList<>();
        Contacts = new ArrayList<>();
        ReminderIgnores = new ArrayList<>();

        activeReminder = null;
    }

    public final int IsActive() { return Active; }
    public final int GetUserID() { return UserID; }
    public final String GetFullName() { return FullName; }
    public final String GetUsername() { return Username; }
    public final String GetEmail() { return Email; }
    public final String GetPassword() { return Password; }
    public final int GetCoins() { return Coins; }
    public final boolean IsIgnoring(int id) { return ReminderIgnores.contains(id); }
    public final Reminder GetActiveReminder() { return activeReminder; }

    public List<Reminder> GetReminders() { return Reminders; }
    public List<ContactProfile> GetContacts() { return Contacts; }

    public void SetActiveReminder(Reminder reminder)
    {
        activeReminder = reminder;
    }
    public void SetIgnoreReminder(int id, boolean value)
    {
        if (value)
        {
            if (!ReminderIgnores.contains(id))
                ReminderIgnores.add(id);
        }
        else
        {
            if (ReminderIgnores.contains(id))
                ReminderIgnores.remove(Integer.valueOf(id));
        }

        SaveReminderIgnoresToFile(UserAreaActivity.GetActivity());
    }

    protected UserProfile(Parcel in)
    {
        Active = in.readInt();
        FullName = in.readString();
        Username = in.readString();
        Email = in.readString();
        Password = in.readString();
        Coins = in.readInt();
    }

    private void Update(final int id, final int active, final String fullName, final String username, final String email, final String password, final Integer coins)
    {
        UserID = id;
        Active = active;
        FullName = fullName;
        Username = username;
        Email = email;
        Password = password;
        Coins = coins;
    }

    public void RefreshReminderLayout()
    {
        ResetLinearLayout(UserAreaActivity.GetActivity().reminderLayout);

        if (GetReminders().isEmpty() && UserAreaActivity.GetActivity() != null)
        {
            TextView tv = new TextView(UserAreaActivity.GetActivity());
            tv.setText("No reminders!");
            tv.setTextSize(18f);
            tv.setAllCaps(true);
            tv.setPadding(5, 15, 5, 15);
            UserAreaActivity.GetActivity().reminderLayout.addView(tv);

            return;
        }

        Collections.sort(GetReminders());

        for (Reminder r : GetReminders())
        {
            UserAreaActivity.GetActivity().reminderLayout.addView(r.CreateWidget(UserAreaActivity.GetActivity(), UserAreaActivity.GetActivity().reminderLayout));
        }

    }

    public Set<ContactProfile> GetContactSet()
    {
        Set<ContactProfile> set = new HashSet<>();
        for (ContactProfile contact : GetContacts())
        {
            set.add(contact);
        }
        return set;
    }

    public Set<String> GetContactStringSet()
    {
        Set<String> set = new HashSet<>();
        for (ContactProfile contact : GetContacts())
        {
            set.add(contact.toString());
        }
        return set;
    }

    private void ResetLinearLayout(LinearLayout layout)
    {
        for (Reminder r : GetReminders())
        {
            r.SetWidget(null);
        }
        layout.removeAllViews();
    }

    public void DeleteReminder(Reminder r)
    {
        Log.d("INFO", "Deleting reminder...");
        if (UserAreaActivity.GetActivity() == null)
        {
            Log.d("SEVERE", "UserAreaActivity is NULL...");
            return;
        }
        Response.Listener<String> responseListener = r.GetDeleteResponseListener(UserAreaActivity.GetActivity());

        DeleteReminderRequest request = new DeleteReminderRequest(GetUserID(), GetPassword(), r.GetID(), responseListener);
        RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.GetActivity());
        queue.add(request);

        Reminders.remove(r);
        SaveRemindersToFile(UserAreaActivity.GetActivity());
        RefreshReminderLayout();
    }

    public void AddReminder(Reminder r)
    {
        Reminders.add(r);
        //RefreshReminderLayout((Activity) r.GetParent().getContext(), r.GetParent());
    }

    public Reminder GetReminder(int id)
    {
        for (Reminder r : GetReminders())
            if (r.GetID() == id)
                return r;
        return null;
    }

    public void pushReminder(Reminder r)
    {
        //TODO: Send changes to server.
        Response.Listener<String> responseListener = r.GetUpdateResponseListener(UserAreaActivity.GetActivity());
        UpdateReminderRequest request = new UpdateReminderRequest(r, responseListener);
        RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.GetActivity());
        queue.add(request);
    }

    public void AddContact(ContactProfile profile)
    {
        GetContacts().add(profile);

        //TODO: SEND REQUEST TO SERVER
    }

    public void RemoveContact(int _id)
    {
        for (int i = 0; i < GetContacts().size(); i++)
        {
            ContactProfile p = GetContacts().get(i);
            if (p.GetID() == _id)
            {
                GetContacts().remove(Integer.valueOf(i));
                i--;
            }
        }
    }

    public void Pull(Activity activity)
    {
        Log.d("INFO", "Pulling profile from server...");
        Response.Listener<String> profileResponseListener = GetPullResponseListener();
        Response.Listener<String> reminderResponseListener = Reminder.GetReceivedResponseListener();

        LoginRequest request = new LoginRequest(Email, Password, profileResponseListener);
        GetRemindersRequest request1 = new GetRemindersRequest(reminderResponseListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
        queue.add(request1);
    }

    private Response.Listener<String> GetPullResponseListener()
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

                    if (success)
                    {
                        final int id = jsonResponse.getInt("userID");
                        final int active = jsonResponse.getInt("active");

                        final String fullName = jsonResponse.getString("fullname");
                        final String email = jsonResponse.getString("email");
                        final String username = jsonResponse.getString("username");

                        final int coins = jsonResponse.getInt("coins");

                        final String contacts = jsonResponse.getString("contacts");

                        if (UserProfile.PROFILE != null && coins != UserProfile.PROFILE.GetCoins())
                        {
                            ShowCoinGainScheduler.Initialize();
                        }

                        if (UserProfile.PROFILE == null)
                            UserProfile.PROFILE = new UserProfile(id, active, fullName, username, email, Password, coins);
                        else
                            UserProfile.PROFILE.Update(id, active, fullName, username, email, Password, coins);
                        UserProfile.PROFILE.GetContacts().clear();
                        for (String contact : contacts.split("&"))
                        {
                            if (contact == "" || contact == " ")
                                continue;
                            String[] key = contact.split("%");
                            if (key[0].equalsIgnoreCase("0"))
                            {
                                UserProfile.PROFILE.AddContact(new ContactProfile(Integer.parseInt(key[1]), key[2]));
                            }
                            else if (key[0].equalsIgnoreCase("1"))
                            {
                                if (key.length >= 6)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], key[5]));
                                else if (key.length == 5)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], ""));
                            }


                        }

                        if (active != 1 && !ActivateAccountActivity.IsOpen())
                        {
                            Intent activateIntent = new Intent(UserAreaActivity.GetActivity(), ActivateAccountActivity.class);
                            UserAreaActivity.GetActivity().startActivity(activateIntent);
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
        parcel.writeInt(Coins);
    }

    public void LoadRemindersFromFile(Activity activity)
    {
        final String filename = "reminders.yml";

        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        File file = new File(activity.getBaseContext().getFilesDir(), filename);
        Map<String, ArrayList<String>> data;

        if (!file.exists())
            return;

        try
        {

            InputStream input = new FileInputStream(file);
            Yaml config = new Yaml();
            for (Object o : config.loadAll(input))
            {
                data = (Map) o;
                for (String key : data.keySet())
                {
                    String[] rArray = data.get(key).toArray(new String[data.get(key).size()]);
                    int id = Integer.parseInt(rArray[0]);
                    int from = Integer.parseInt(rArray[1]);
                    int to = Integer.parseInt(rArray[2]);
                    String message = rArray[3];
                    Date date = formatter.parse(rArray[4]);
                    boolean important = (rArray[5].equalsIgnoreCase("1")) ? true : false;
                    ReminderState rState = ReminderState.values()[Integer.parseInt(rArray[6])];

                    Reminder.LoadReminder(true, id, from, to, message, important, date, rState);
                }
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public  void SaveRemindersToFile(Activity activity)
    {
        final String filename = "reminders.yml";

        File file = new File(activity.getBaseContext().getFilesDir(), filename);

        if (file.exists())
            file.delete();

        Map<String, String[]> data = new HashMap<>();
        for (Reminder r : GetReminders())
        {
            data.put("id-" + r.GetID(), r.toArray());
        }

        Yaml config = new Yaml();
        FileWriter writer;
        try
        {
            writer = new FileWriter(file);
            config.dump(data, writer);
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }


    }

    public void SaveReminderIgnoresToFile(Activity activity)
    {
        final String filename = "ignores.yml";

        File file = new File(activity.getBaseContext().getFilesDir(), filename);

        if (file.exists())
            file.delete();

        Integer[] ignores = ReminderIgnores.toArray(new Integer[ReminderIgnores.size()]);

        Yaml config = new Yaml();
        FileWriter writer;
        try
        {
            writer = new FileWriter(file);
            config.dump(ignores, writer);
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public void LoadReminderIgnoresFromFile(Activity activity)
    {
        final String filename = "ignores.yml";

        File file = new File(activity.getBaseContext().getFilesDir(), filename);
        List<Integer> ignores;

        if (!file.exists())
            return;

        try
        {

            InputStream input = new FileInputStream(file);
            Yaml config = new Yaml();
            for (Object o : config.loadAll(input))
            {
                ignores = (ArrayList) o;

                for (Integer i : ignores)
                {
                    ReminderIgnores.add(i);
                }
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
