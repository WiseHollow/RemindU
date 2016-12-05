package net.johnbrooks.remindu.util;

/**
 * Created by John on 11/28/2016.
 */

public class AcceptedContactProfile extends ContactProfile
{
    private String FullName, Username, Contacts;

    public AcceptedContactProfile(int id, String email, String fullName, String username, String contacts)
    {
        super(id, email);
        FullName = fullName;
        Username = username;
        Contacts = contacts;
    }
    @Override
    public final String GetFullName() { return FullName; }
    @Override
    public final String GetUsername() { return Username; }
    @Override
    public String GetDisplayName() { return FullName; }
    @Override
    public String GetContacts() { return Contacts; }
    @Override
    public final boolean IsContact()
    {
        for (String s : Contacts.split(" "))
            if (Integer.parseInt(s) == UserProfile.PROFILE.GetUserID())
                return true;
        return false;
    }
}
