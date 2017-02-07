package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.fragments.FeedFragment;
import net.johnbrooks.remindu.fragments.PrimaryFragment;
import net.johnbrooks.remindu.requests.DeleteReminderRequest;
import net.johnbrooks.remindu.requests.GetRemindersRequest;
import net.johnbrooks.remindu.requests.PullProfileRequest;
import net.johnbrooks.remindu.requests.UpdateReminderRequest;
import net.johnbrooks.remindu.schedulers.MasterScheduler;

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
    public static void SaveCredentials(Activity activity, String email, String password, String fullName, String username, int id, int active, int coins)
    {
        SharedPreferences sharedPref = activity.getSharedPreferences("profile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("fullname", fullName);
        editor.putString("username", username);
        editor.putInt("id", id);
        editor.putBoolean("active", (active > 0) ? true : false);
        editor.putInt("coins", coins);
        editor.putStringSet("contacts", UserProfile.PROFILE.GetContactStringSet());
        editor.putString("avatar", UserProfile.PROFILE.GetAvatarID());

        editor.commit();
    }

    public static UserProfile PROFILE = null;

    private int UserID;
    private int Active;

    private String AvatarID;

    private String FullName;
    private String Username;
    private String Email;
    private String Password;

    private int Coins;

    private List<ContactProfile> Contacts;
    private List<Reminder> Reminders;
    private List<Integer> ReminderIgnores;

    private Reminder activeReminder;

    public boolean sortRemindersByDueDate;

    public UserProfile(int id, final int active, final String fullName, final String username, final String email, final String password, final Integer coins, final String avatarID)
    {
        UserID = id;
        Active = active;
        FullName = fullName;
        Username = username;
        Email = email;
        Password = password;
        Coins = coins;
        AvatarID = avatarID;

        Reminders = new ArrayList<>();
        Contacts = new ArrayList<>();
        ReminderIgnores = new ArrayList<>();

        activeReminder = null;
        sortRemindersByDueDate = true;
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
    public final String GetAvatarID() { return AvatarID; }
    public final List<Reminder> GetActiveSentReminders()
    {
        List<Reminder> list = new ArrayList<>();

        for (Reminder r : UserProfile.PROFILE.GetReminders())
        {
            if (r.GetFrom() == UserProfile.PROFILE.GetUserID())
                list.add(r);
        }

        return list;
    }
    public final List<Reminder> GetActiveReceivedReminders()
    {
        List<Reminder> list = new ArrayList<>();

        for (Reminder r : UserProfile.PROFILE.GetReminders())
        {
            if (r.GetTo() == UserProfile.PROFILE.GetUserID())
                list.add(r);
        }

        return list;
    }

    public List<Reminder> GetReminders() { return Reminders; }
    public List<ContactProfile> GetContacts() { return Contacts; }

    public void SetAvatarID(String value) { AvatarID = value; }

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

        SaveReminderIgnoresToFile();
    }

    protected UserProfile(Parcel in)
    {
        Active = in.readInt();
        FullName = in.readString();
        Username = in.readString();
        Email = in.readString();
        Password = in.readString();
        Coins = in.readInt();
        AvatarID = in.readString();
    }

    public void Update(final int id, final int active, final String fullName, final String username, final String email, final String password, final Integer coins, final String avatarID)
    {
        UserID = id;
        Active = active;
        FullName = fullName;
        Username = username;
        Email = email;
        Password = password;
        Coins = coins;
        AvatarID = avatarID;
    }

    public void RefreshReminderLayout()
    {
        //if (UserAreaActivity.GetActivity() == null)
        //    return;

        ResetLinearLayout();

        Collections.sort(GetReminders());
        Collections.sort(GetContacts());

        char latestChar = '-';
        ((ViewGroup) PrimaryFragment.GetInstance().ContactLayout).removeAllViews();

        for (int i = 0; i < GetContacts().size(); i++)
        {
            ContactProfile cp = GetContacts().get(i);
            if (cp != null)
            {
                if (latestChar != cp.GetFullName().charAt(0))
                {
                    LinearLayout sep = (LinearLayout) UserAreaActivity.GetActivity().getLayoutInflater().inflate(R.layout.widget_alphabet_separator, null);
                    TextView character = (TextView) sep.findViewById(R.id.Alphabet_Separator);
                    character.setText("" + cp.GetFullName().charAt(0));
                    ((ViewGroup) PrimaryFragment.GetInstance().ContactLayout).addView(sep);
                }

                latestChar = cp.GetFullName().charAt(0);
                View view = cp.CreateCategoryWidget(UserAreaActivity.GetActivity());

                ((ViewGroup) PrimaryFragment.GetInstance().ContactLayout).addView(view);

                if (i % 2 != 0)
                    view.setBackgroundColor(Color.parseColor("#eaf7ff"));
                else
                    view.setBackgroundColor(Color.parseColor("#FCFCFC"));
            }
        }

        if (FeedFragment.GetInstance() != null)
            FeedFragment.GetInstance().PopulateActivity();
    }

    public ContactProfile GetContact(int id)
    {
        for (ContactProfile cp : GetContacts())
            if (cp.GetID() == id)
                return cp;
        return null;
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

    private void ResetLinearLayout()
    {
        for (Reminder r : GetReminders())
        {
            r.SetWidget(null);
        }
    }

    public void DeleteReminder(Reminder r)
    {
        Log.d("INFO", "Deleting reminder...");
        if (UserAreaActivity.GetActivity() == null)
        {
            Log.d("SEVERE", "UserAreaActivity is NULL...");
            return;
        }

        DeleteReminderRequest.SendRequest(r);

        Reminders.remove(r);
        SaveRemindersToFile();
        RefreshReminderLayout();
    }

    public void AddReminder(Reminder r)
    {
        Reminders.add(r);
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
        UpdateReminderRequest.SendRequest(r);
    }

    public void AddContact(ContactProfile profile)
    {
        GetContacts().add(profile);
    }

    public void RemoveContact(int _id)
    {
        for (int i = 0; i < GetContacts().size(); i++)
        {
            ContactProfile p = GetContacts().get(i);
            if (p.GetID() == _id)
            {
                GetContacts().remove(p);
                i--;
            }
        }
    }

    public void Pull()
    {
        if (MasterScheduler.GetInstance().GetActivity() != null)
        {
            if (!Network.IsConnected(MasterScheduler.GetInstance().GetActivity()))
                return;
            Log.d("INFO", "Pulling profile from server...");
            PullProfileRequest.SendRequest(MasterScheduler.GetInstance().GetActivity());
            Log.d("INFO", "Pulling reminders from server...");
            GetRemindersRequest.SendRequest(MasterScheduler.GetInstance().GetActivity());
        }
        else if (MasterScheduler.GetInstance().GetService() != null)
        {
            if (!Network.IsConnected(MasterScheduler.GetInstance().GetService()))
                return;
            Log.d("INFO", "Pulling profile from server...");
            PullProfileRequest.SendRequest(MasterScheduler.GetInstance().GetService());
            Log.d("INFO", "Pulling reminders from server...");
            GetRemindersRequest.SendRequest(MasterScheduler.GetInstance().GetService());
        }
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
        parcel.writeString(AvatarID);
    }

    public void ProcessReminders()
    {
        for (Reminder r : GetReminders())
        {
            r.ProcessReminderNotifications();
        }

        Log.d("INFO", "Finished processing current reminders.");
    }

    public void LoadRemindersFromFile(Activity activity)
    {
        final String filename = "reminders.yml";

        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        File file = new File(activity.getBaseContext().getFilesDir(), filename);
        //Log.d("INFO", "Base Context File Dir: " + file.getAbsolutePath());
        Map<String, ArrayList<String>> data;

        if (!file.exists())
        {
            Log.d("INFO", "Loading reminders failed. " + filename + " does not exist. ");
            return;
        }

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
                    Reminder.ReminderState rState = Reminder.ReminderState.values()[Integer.parseInt(rArray[6])];

                    String dateInProgress = rArray[7];
                    String dateCompleted = rArray[8];

                    Reminder r = Reminder.LoadReminder(true, id, from, to, message, important, date, rState);
                    if (dateInProgress != null)
                        r.SetDateInProgress(dateInProgress);
                    if (dateCompleted != null)
                        r.SetDateComplete(dateCompleted);
                }
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (ParseException e)
        {
            e.printStackTrace();
        }

        Log.d("INFO", "Loaded all reminders from file.");
    }

    public void LoadRemindersFromFile(Service service)
    {
        final String filename = "reminders.yml";

        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        File file = new File(service.getBaseContext().getFilesDir(), filename);
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
                    Reminder.ReminderState rState = Reminder.ReminderState.values()[Integer.parseInt(rArray[6])];

                    String dateInProgress = rArray[7];
                    String dateCompleted = rArray[8];

                    Reminder r = Reminder.LoadReminder(true, id, from, to, message, important, date, rState);
                    if (!dateInProgress.equalsIgnoreCase("null"))
                        r.SetDateInProgress(dateInProgress);
                    if (!dateCompleted.equalsIgnoreCase("null"))
                        r.SetDateComplete(dateCompleted);
                }
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (ParseException e)
        {
            e.printStackTrace();
        }

        Log.d("INFO", "Loaded all reminders from file.");
    }

    public  void SaveRemindersToFile()
    {
        if (MasterScheduler.GetInstance() == null || MasterScheduler.GetInstance().GetActivity() == null)
        {
            Log.d("INFO", "Attempt to save reminders from service. Skipping...");
            return;
        }

        if (GetReminders().size() == 0)
        {
            Log.d("INFO", "No reminders to save. Skipping...");
            return;
        }

        final String filename = "reminders.yml";

        File file = new File(UserAreaActivity.GetActivity().getBaseContext().getFilesDir(), filename);

        if (file.exists())
            file.delete();

        Log.d("INFO", file.getAbsolutePath());

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

        Log.d("INFO", "Reminders were saved into file.");
    }

    public void SaveReminderIgnoresToFile()
    {
        final String filename = "ignores.yml";
        File file = new File(UserAreaActivity.GetActivity().getBaseContext().getFilesDir(), filename);

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

    public void LoadReminderIgnoresFromFile(Service service)
    {
        if (service == null) { return; }
        final String filename = "ignores.yml";

        File file = new File(service.getBaseContext().getFilesDir(), filename);

        if (!file.exists())
            return;

        List<Integer> ignores;

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
