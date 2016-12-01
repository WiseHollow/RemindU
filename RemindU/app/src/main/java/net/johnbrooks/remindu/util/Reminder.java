package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.johnbrooks.remindu.R;

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

public class Reminder
{
    private LinearLayout Parent;
    private TextView Widget;
    private String Message;
    private Date Date;
    private boolean Important;

    private ReminderState State;

    public Reminder(String message)
    {
        Message = message;
        Date = new Date();
        Important = false;
        State = ReminderState.NOT_STARTED;

        Calendar c = Calendar.getInstance();
        c.setTime(Date);
        c.add(Calendar.DATE, 1);
        c.add(Calendar.HOUR, 6);
        Date = c.getTime();
    }

    public Reminder(String message, boolean important)
    {
        Message = message;
        Date = new Date();
        Important = important;
        State = ReminderState.NOT_STARTED;

        Calendar c = Calendar.getInstance();
        c.setTime(Date);
        c.add(Calendar.DATE, 1);
        c.add(Calendar.HOUR, 6);
        Date = c.getTime();
    }

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
        Bitmap bCheckmark = BitmapFactory.decodeResource( activity.getResources(), R.drawable.checkmark_48 );
        Bitmap bDelete = BitmapFactory.decodeResource( activity.getResources(), R.drawable.delete_48 );
        Bitmap bClock = BitmapFactory.decodeResource( activity.getResources(), R.drawable.clock_48 );
        Bitmap bImportant = BitmapFactory.decodeResource( activity.getResources(), R.drawable.attention_48 );
        int messageLength = GetMessage().toCharArray().length;
        if (GetImportant())
            messageLength+=3;

        //Log.d("TEST", "Size: " + parent.getChildCount());
        int color = Color.LTGRAY;
        if ((parent.getChildCount()) % 2 != 0)
            color = Color.argb(255, 176, 176, 176);

        SpannableStringBuilder buttonContent = new SpannableStringBuilder();
        if (Important)
            buttonContent.append("_  ");
        buttonContent.append(GetMessage());
        buttonContent.append("\n");
        buttonContent.append("_ _ _" + "   " + "Time left: " + GetETA());
        buttonContent.setSpan(new RelativeSizeSpan(1f), 0, messageLength - 1, 0);
        buttonContent.setSpan(new RelativeSizeSpan(0.75f), messageLength + 7, buttonContent.length(), 0);
        buttonContent.setSpan(new ImageSpan(view.getContext(), bCheckmark), messageLength + 1, messageLength + 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        buttonContent.setSpan(new ImageSpan(view.getContext(), bDelete), messageLength + 3, messageLength + 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        buttonContent.setSpan(new ImageSpan(view.getContext(), bClock), messageLength + 5, messageLength + 6, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (GetImportant())
            buttonContent.setSpan(new ImageSpan(view.getContext(), bImportant), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

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
        buttonContent.setSpan(checkMarkClick, messageLength + 1, messageLength + 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        buttonContent.setSpan(deleteClick, messageLength + 3, messageLength + 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        buttonContent.setSpan(clockClick, messageLength + 5, messageLength + 6, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        view.setText(buttonContent);
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

        long months = result.get(TimeUnit.DAYS) / 12;
        long days = result.get(TimeUnit.DAYS);
        long hours = result.get(TimeUnit.HOURS);
        long minutes = result.get(TimeUnit.MINUTES);

        if (months > 0)
            eta += months + " months ";
        if (days > 0)
            eta += days + " days ";
        if (hours > 0)
            eta += hours + " hours ";
        if (minutes > 0)
            eta += minutes + " minutes ";

        return eta;
    }
}

enum ReminderState
{
    NOT_STARTED, IN_PROGRESS, COMPLETE
}