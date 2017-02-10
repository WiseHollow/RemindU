package net.johnbrooks.remindu.util;

/**
 * Created by John on 11/28/2016.
 */

public class AcceptedContactProfile extends ContactProfile
{
    private String FullName, Username, Contacts, AvatarID;

    public AcceptedContactProfile(final int id, final String email, final String fullName, final String username, final String contacts, final String avatarID)
    {
        super(id, email);
        FullName = fullName;
        Username = username;
        Contacts = contacts;
        AvatarID = avatarID;
    }
    @Override
    public final String GetFullName() { return FullName; }
    @Override
    public final String GetShortName()
    {
        String[] names = GetFullName().split(" ");

        String name = names[0];
        if (names.length > 1)
            name += " " + names[1].charAt(0) + ".";

        //TODO: Make length of short name have a max of 10 chars

        return name;
    }
    @Override
    public final String GetEmail() { return Email; }
    @Override
    public final String GetUsername() { return Username; }
    @Override
    public final String GetDisplayName() { return FullName; }
    @Override
    public final String GetContacts() { return Contacts; }
    @Override
    public final String GetAvatarID() { return AvatarID; }
    @Override
    public final boolean IsContact()
    {
        for (String s : Contacts.split(" "))
            if (Integer.parseInt(s) == UserProfile.PROFILE.GetUserID())
                return true;
        return false;
    }
}
