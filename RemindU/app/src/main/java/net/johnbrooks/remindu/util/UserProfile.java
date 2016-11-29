package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.LinearLayout;

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
    public void AddContact(ContactProfile contact) { Contacts.add(contact); }

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
