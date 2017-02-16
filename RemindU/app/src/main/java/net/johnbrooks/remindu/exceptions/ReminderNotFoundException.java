package net.johnbrooks.remindu.exceptions;

/**
 * Created by John on 2/16/2017.
 */

public class ReminderNotFoundException extends Exception
{
    public ReminderNotFoundException(final int id)
    {
        super("Reminder with ID not found: " + id);
    }
}
