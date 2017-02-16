package net.johnbrooks.remindu.util;

import net.johnbrooks.remindu.exceptions.ReminderNotFoundException;

/**
 * Created by John on 2/16/2017.
 */

public class ReminderFlag implements Comparable<ReminderFlag>
{
    public static ReminderFlag Create(int id, Reminder.ReminderState state, boolean liked) throws ReminderNotFoundException
    {
        Reminder reminder = UserProfile.PROFILE.GetReminder(id);
        if (reminder == null)
            throw new ReminderNotFoundException(id);
        ReminderFlag flag = new ReminderFlag(reminder, state, liked);
        reminder.AddFlag(flag);
        return flag;
    }

    private Reminder reminder;
    private Reminder.ReminderState state;
    private boolean liked;

    private ReminderFlag(Reminder reminder, Reminder.ReminderState state, boolean liked)
    {
        this.reminder = reminder;
        this.state = state;
        this.liked = liked;
    }

    public final boolean IsLiked() { return liked; }
    public final Reminder GetReminder() { return reminder; }
    public final Reminder.ReminderState GetState() { return state; }
    public final String GetDateOfFlag()
    {
        if (state == Reminder.ReminderState.IN_PROGRESS)
            return reminder.GetDateInProgress();
        else if (state == Reminder.ReminderState.COMPLETE)
            return reminder.GetDateComplete();
        else
            return null;
    }

    public final void SetLiked(boolean liked) { this.liked = liked; }

    public final String[] toArray()
    {
        return new String[]{ String.valueOf(reminder.GetID()),
                String.valueOf(state.ordinal()),
                liked ? "1" : "0"
        };
    }

    @Override
    public int compareTo(ReminderFlag o)
    {
        String d1 = (GetDateOfFlag() != null) ? GetDateOfFlag() : "";
        String d2 = (o.GetDateOfFlag() != null) ? o.GetDateOfFlag() : "";

        return d2.compareTo(d1);
    }
}
