package net.johnbrooks.remindu.util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by John on 11/24/2016.
 */

public class UserProfile implements Parcelable
{
    private int Active;

    private String FullName;
    private String Username;
    private String Email;
    private String Password;

    private int PointsRemaining;
    private int PointsSent;
    private int PointsReceived;

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
    }

    public final int IsActive() { return Active; }
    public final String GetFullName() { return FullName; }
    public final String GetUsername() { return Username; }
    public final String GetEmail() { return Email; }
    public final String GetPassword() { return Password; }
    public final int GetPointsRemaining() { return PointsRemaining; }
    public final int GetPointsSent() { return PointsSent; }
    public final int GetPointsReceived() { return PointsReceived; }

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
