package net.johnbrooks.remindu.util;

/**
 * Created by John on 11/28/2016.
 */

public class ContactProfile
{
    private int ID;
    private String FullName, Username, Email, Contacts;

    public ContactProfile(int id, String fullName, String username, String email, String contacts)
    {
        ID = id;
        FullName = fullName;
        Username = username;
        Email = email;
        Contacts = contacts;
    }
    public final int GetID() { return ID; }
    public final String GetFullName() { return FullName; }
    public final String GetUsername() { return Username; }
    public final String GetEmail() { return Email; }
}
