package net.johnbrooks.remindu.util;

/**
 * Created by John on 11/29/2016.
 */

public class ContactProfile
{
    public static ContactProfile GetProfile(int user_id)
    {
        for (ContactProfile cp : UserProfile.PROFILE.GetContacts())
            if (cp.GetID() == user_id)
                return cp;
        return null;
    }

    private int ID;
    private String Email;

    public ContactProfile(int id, String email)
    {
        ID = id;
        Email = email;
    }
    public final int GetID() { return ID; }
    public final String GetEmail() { return Email; }
    public String GetUsername() { return "null"; }
    public String GetFullName() { return "null"; }
    public String GetDisplayName() { return Email; }
    public String GetContacts() { return "null"; }
    public boolean IsContact()
    {
        if (this.getClass() == AcceptedContactProfile.class)
            return true;
        else
            return false;
    }
    @Override
    public String toString()
    {
        return GetID() + "%" + GetEmail() + "%" + GetUsername() + "%" + GetFullName() + "%" + GetContacts();
    }
}
