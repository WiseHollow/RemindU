package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.UserAreaActivity;
import net.johnbrooks.remindu.requests.SendReminderRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by John on 11/28/2016.
 */

public class Reminder implements Comparable<Reminder>
{
    public static Response.Listener<String> GetReceivedResponseListener()
    {
        return new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    String errorMessage = jsonResponse.getString("message");

                    Log.d("INFO", "Received response: " + success);

                    if (success)
                    {
                        int size = jsonResponse.getInt("size");
                        UserProfile.PROFILE.GetReminders().clear();

                        for(int i = 0; i < size; i++)
                        {
                            String message = jsonResponse.getJSONObject(String.valueOf(i)).getString("message");
                            int state = jsonResponse.getJSONObject(String.valueOf(i)).getInt("state");
                            int important = jsonResponse.getJSONObject(String.valueOf(i)).getInt("important");
                            String dateString = jsonResponse.getJSONObject(String.valueOf(i)).getString("date");
                            DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
                            Date date = formatter.parse(dateString);

                            int from = jsonResponse.getJSONObject(String.valueOf(i)).getInt("user_id_from");
                            int to = jsonResponse.getJSONObject(String.valueOf(i)).getInt("user_id_to");

                            Reminder.LoadReminder(from, to, message, important > 0 ? true : false, date);
                        }

                        UserProfile.PROFILE.RefreshReminderLayout();
                    }
                    else
                    {
                        Log.d("ERROR", "Message: " + errorMessage);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /** Insert a reminder into memory, and upload the request to the user's online records. */
    public static Reminder CreateReminder(int user_id_to, String message, boolean important, Date date, Activity activity)
    {
        Reminder reminder = new Reminder(message, UserProfile.PROFILE.GetUserID(), user_id_to, date);
        reminder.SetImportant(important);
        UserProfile.PROFILE.AddReminder(reminder);

        SendReminderRequest request = new SendReminderRequest(UserProfile.PROFILE.GetUserID(), user_id_to, UserProfile.PROFILE.GetPassword(), message, important, date, reminder.GetSendResponseListener(activity));
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);

        return reminder;
    }
    /** Insert a reminder into memory (local only). */
    public static Reminder LoadReminder(int user_id_from, int user_id_to, String message, boolean important, Date date)
    {
        Reminder reminder = new Reminder(message, user_id_from, user_id_to, date);
        reminder.SetImportant(important);
        UserProfile.PROFILE.AddReminder(reminder);

        return reminder;
    }

    private int User_ID_From;
    private int User_ID_To;
    private String FullName = null;
    private LinearLayout Parent;
    private TextView Widget;
    private String Message;
    private Date Date;
    private boolean Important;

    private ReminderState State;

    private Reminder(String message, int user_id_from, int user_id_to, Date date)
    {
        User_ID_From = user_id_from;
        User_ID_To = user_id_to;

        try
        {
            if (user_id_from == UserProfile.PROFILE.GetUserID())
                FullName = ContactProfile.GetProfile(user_id_to).GetFullName();
            else
                FullName = ContactProfile.GetProfile(user_id_from).GetFullName();
        }
        catch(Exception ex)
        {
            FullName = "N/A";
        }

        Message = message;
        Date = date;
        Important = false;
        State = ReminderState.NOT_STARTED;

        Calendar c = Calendar.getInstance();
        c.setTime(Date);
        c.add(Calendar.DATE, 1);
        c.add(Calendar.HOUR, 6);
        Date = c.getTime();
    }

    public int GetFrom() { return User_ID_From; }
    public int GetTo() { return User_ID_To; }
    public String GetFullName() { return FullName; }
    public String GetMessage() { return Message; }
    public Date GetDate() { return Date; }
    public boolean GetImportant() { return Important; }
    public ReminderState GetState() { return State; }
    public LinearLayout GetParent() { return Parent; }

    public void SetImportant(boolean value) { Important = value; }
    public void SetState(ReminderState state) { State = state; }
    public void SetWidget(TextView To) { Widget = To; }
    public TextView CreateWidget(final Activity activity, LinearLayout parent)
    {
        if (Widget != null)
            return Widget;

        Parent = parent;

        final Reminder reminder = this;
        TextView view = new TextView(activity);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        Bitmap bState = BitmapFactory.decodeResource( activity.getResources(), R.drawable.running_96 );
        Bitmap bDelete = BitmapFactory.decodeResource( activity.getResources(), R.drawable.delete_96 );
        Bitmap bMute = BitmapFactory.decodeResource( activity.getResources(), R.drawable.mute_96 );
        Bitmap bImportant = BitmapFactory.decodeResource( activity.getResources(), R.drawable.attention_54 );
        Bitmap bBack = BitmapFactory.decodeResource( activity.getResources(), R.drawable.back_arrow_48 );
        Bitmap bForward = BitmapFactory.decodeResource( activity.getResources(), R.drawable.forward_arrow_48 );
        int messageLength = GetMessage().toCharArray().length;
        if (GetImportant())
            messageLength+=3;

        int color = Color.LTGRAY;
        if ((parent.getChildCount()) % 2 != 0)
            color = Color.argb(255, 176, 176, 176);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        String line1 = "";
        if (GetImportant())
            line1 = "_ ";
        line1 +=  "_ " + GetFullName() + System.getProperty("line.separator");
        String line2 = "Message: " + GetMessage() + System.getProperty("line.separator");
        String line3 = "Time left: " + GetETA() + System.getProperty("line.separator");
        String line4 = "_ _ _";

        spannableStringBuilder.append(line1);
        spannableStringBuilder.append(line2);
        spannableStringBuilder.append(line3);
        spannableStringBuilder.append(line4);

        /*if (Important)
            spannableStringBuilder.append("_  ");
        spannableStringBuilder.append(GetMessage());
        spannableStringBuilder.append("\n");
        spannableStringBuilder.append("_ _ _" + "   " + "Time left: " + GetETA());*/
        spannableStringBuilder.setSpan(new RelativeSizeSpan(1.5f), 0, line1.length() - 1, 0);
        spannableStringBuilder.setSpan(new RelativeSizeSpan(1f), line1.length(), line1.length() + line2.length() + 1, 0);

        int offset = 0;
        if (GetImportant())
            offset = 2;
        if (GetFrom() == UserProfile.PROFILE.GetUserID())
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bForward), offset, offset + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        else
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bBack), offset, offset + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bState), line1.length() + line2.length() + line3.length(), line1.length() + line2.length() + line3.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bDelete), line1.length() + line2.length() + line3.length() + 2, line1.length() + line2.length() + line3.length() + 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bMute), line1.length() + line2.length() + line3.length() + 4, line1.length() + line2.length() + line3.length() + 5, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (GetImportant())
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bImportant), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, line1.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), line1.length(), line1.length() + 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), line1.length() + line2.length(), line1.length() + line2.length() + 10, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        ClickableSpan checkMarkClick = new ClickableSpan() {
            @Override
            public void onClick(View view)
            {
                //TODO: Mark as complete
                //TODO: Open dialog giving options of progress.
                SetState(ReminderState.COMPLETE);
            }
        };
        ClickableSpan deleteClick = new ClickableSpan()
        {
            @Override
            public void onClick(View view)
            {
                UserProfile.PROFILE.deleteReminder(reminder);
            }
        };
        ClickableSpan clockClick = new ClickableSpan() {
            @Override
            public void onClick(View view)
            {
                //TODO: Mark as complete
                //TODO: Open dialog giving options of progress.
            }
        };
        spannableStringBuilder.setSpan(checkMarkClick, line1.length() + line2.length() + line3.length(), line1.length() + line2.length() + line3.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(deleteClick, line1.length() + line2.length() + line3.length() + 2, line1.length() + line2.length() + line3.length() + 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(clockClick, line1.length() + line2.length() + line3.length() + 4, line1.length() + line2.length() + line3.length() + 5, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        view.setText(spannableStringBuilder);
        view.setPadding(10, 12, 10, 12);
        view.setBackgroundColor(color);



        Widget = view;
        return view;
    }
    private String GetETA()
    {
        Date now = new Date();
        String eta = "";

        long differenceInMilliseconds = Date.getTime() - now.getTime();
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);
        Map<TimeUnit,Long> result = new LinkedHashMap<TimeUnit,Long>();
        long millisecondsLeft = differenceInMilliseconds;
        for ( TimeUnit unit : units ) {
            long diff = unit.convert(millisecondsLeft,TimeUnit.MILLISECONDS);
            long differenceInMillisecondsForUnit = unit.toMillis(diff);
            millisecondsLeft = millisecondsLeft - differenceInMillisecondsForUnit;
            result.put(unit,diff);
        }

        long days = result.get(TimeUnit.DAYS);
        long hours = result.get(TimeUnit.HOURS);
        long minutes = result.get(TimeUnit.MINUTES);

        if (days > 0)
            eta += days + " days ";
        if (hours > 0)
            eta += hours + " hours ";
        if (minutes > 0)
            eta += minutes + " minutes ";

        return eta;
    }

    private Response.Listener<String> GetSendResponseListener(final Activity activity)
    {
        return new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    Log.d("INFO", "Received response: " + success);

                    if (success)
                    {
                        //TODO: Pull Reminders from server and refresh user area.
                        activity.finish();
                    }
                    else
                    {
                        Log.d("ERROR", "Message: " + message);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public int compareTo(Reminder to)
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateString1 = formatter.format(GetDate());
        String dateString2 = formatter.format(to.GetDate());

        return dateString1.compareTo(dateString2);
    }
}

enum ReminderState
{
    NOT_STARTED, IN_PROGRESS, COMPLETE
}