package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.LinearLayout;

import net.johnbrooks.remindu.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 11/24/2016.
 */

public class UserProfile implements Parcelable
{
    public static UserProfile PROFILE = null;

    private int Active;

    private String FullName;
    private String Username;
    private String Email;
    private String Password;

    private int PointsRemaining;
    private int PointsSent;
    private int PointsReceived;

    private List<Reminder> Reminders;

    public UserProfile(int active, String fullName, String username, String email, String password, Integer pointsRemaining, Integer pointsReceived, Integer pointsSent)
    {
        Active = active;
        FullName = fullName;
        Username = username;
        Email = email;
        Password = password;
        PointsReceived = pointsReceived;
        PointsRemaining = pointsRemaining;
        PointsSent = pointsSent;

        Reminders = new ArrayList<>();

        Reminder test = new Reminder("This is a test reminder.");
        Reminder test2 = new Reminder("Make sure to take out the trash. ");
        test2.SetImportant(true);

        Reminders.add(test);
        Reminders.add(test2);
    }

    public final int IsActive() { return Active; }
    public final String GetFullName() { return FullName; }
    public final String GetUsername() { return Username; }
    public final String GetEmail() { return Email; }
    public final String GetPassword() { return Password; }
    public final int GetPointsRemaining() { return PointsRemaining; }
    public final int GetPointsSent() { return PointsSent; }
    public final int GetPointsReceived() { return PointsReceived; }

    public List<Reminder> GetReminders() { return Reminders; }

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
        layout.removeAllViews();

        for (Reminder r : GetReminders())
        {
            layout.addView(r.CreateWidget(activity, layout));
        }
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
