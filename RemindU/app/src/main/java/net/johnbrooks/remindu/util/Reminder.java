package net.johnbrooks.remindu.util;

/**
 * Created by John on 11/28/2016.
 */

public class Reminder
{
    private String Message;
    private ReminderDate Date;
    private boolean Important;

    private int Progress; // 0 - 100

    public Reminder(String message)
    {
        Message = message;
        Date = new ReminderDate();
        Important = false;
        Progress = 0;
    }

    public String GetMessage() { return Message; }
    public ReminderDate GetDate() { return Date; }
    public boolean GetImportant() { return Important; }
    public int GetProgress() { return Progress; }

    public void SetImportant(boolean value) { Important = value; }
    public void SetProgress(int updatedProgress) { Progress = updatedProgress; }

}

class ReminderDate
{
    public int Year, Month, Day, Hours, Minutes;
    public ReminderDate()
    {
        Year = 2016;
        Month = 11;
        Day = 30;
        Hours = 13;
        Minutes = 26;
    }
    public ReminderDate(int year, int month, int day, int hours, int minutes)
    {
        Year = year;
        Month = month;
        Day = day;
        Hours = hours;
        Minutes = minutes;
    }
}