package net.johnbrooks.remindu.util;

/**
 * Created by John on 11/28/2016.
 */

public class AcceptedContactProfile extends ContactProfile
{
    private String FullName, Email, Contacts, AvatarID;
    private int Reputation;

    public AcceptedContactProfile(final int id, final String username, final String fullName, final String email, final String contacts, final String avatarID, final int reputation)
    {
        super(id, username);
        FullName = fullName;
        Email = email;
        Contacts = contacts;
        AvatarID = avatarID;
        Reputation = reputation;
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
        if (GetID() == -1)
            return true;
        if (Contacts.equalsIgnoreCase(""))
            return false;
        for (String s : Contacts.split(" "))
            if (Integer.parseInt(s) == UserProfile.PROFILE.GetUserID())
                return true;
        return false;
    }

    @Override
    public final int GetReputation()
    {
        return Reputation;
    }

    public final void SetReputation(final int Rep)
    {
        Reputation = Rep;
    }
}
