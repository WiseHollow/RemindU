package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.UserAreaActivity;
import net.johnbrooks.remindu.requests.SendReminderRequest;
import net.johnbrooks.remindu.schedulers.PullScheduler;

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

import static android.content.Context.NOTIFICATION_SERVICE;

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
                    for (Reminder r : UserProfile.PROFILE.GetReminders())
                        r.Old = true;
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    String errorMessage = jsonResponse.getString("message");

                    Log.d("INFO", "Received response: " + success);

                    if (success)
                    {
                        int size = jsonResponse.getInt("size");

                        for(int i = 0; i < size; i++)
                        {
                            int id = jsonResponse.getJSONObject(String.valueOf(i)).getInt("id");

                            String message = jsonResponse.getJSONObject(String.valueOf(i)).getString("message");
                            int state = jsonResponse.getJSONObject(String.valueOf(i)).getInt("state");
                            int important = jsonResponse.getJSONObject(String.valueOf(i)).getInt("important");
                            String dateString = jsonResponse.getJSONObject(String.valueOf(i)).getString("date");
                            DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                            Date date = formatter.parse(dateString);

                            int from = jsonResponse.getJSONObject(String.valueOf(i)).getInt("user_id_from");
                            int to = jsonResponse.getJSONObject(String.valueOf(i)).getInt("user_id_to");

                            String dateInProgress = jsonResponse.getJSONObject(String.valueOf(i)).getString("date_in_progress");
                            String dateComplete = jsonResponse.getJSONObject(String.valueOf(i)).getString("date_complete");

                            Reminder r = Reminder.LoadReminder(false, id, from, to, message, important > 0 ? true : false, date, ReminderState.values()[state]);
                            if (!dateInProgress.equalsIgnoreCase("null"))
                                r.SetDateInProgress(dateInProgress);
                            if (!dateComplete.equalsIgnoreCase("null"))
                                r.SetDateComplete(dateComplete);
                        }

                        UserProfile.PROFILE.RefreshReminderLayout();
                        UserProfile.PROFILE.SaveRemindersToFile(UserAreaActivity.GetActivity());
                    }
                    else
                    {
                        Log.d("ERROR", "Message: " + errorMessage);
                    }

                    for (Reminder r : UserProfile.PROFILE.GetReminders())
                        if (r.Old == true && r.GetID() != 0)
                            UserProfile.PROFILE.DeleteReminder(r);
                } catch (JSONException e)
                {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /** Upload the request to the user's online records. */
    public static Reminder CreateReminder(int user_id_to, String message, boolean important, Date date, Activity activity)
    {
        Reminder reminder = new Reminder(message, UserProfile.PROFILE.GetUserID(), user_id_to, date);
        reminder.SetImportant(important);
        //UserProfile.PROFILE.AddReminder(reminder);

        SendReminderRequest request = new SendReminderRequest(UserProfile.PROFILE.GetUserID(), user_id_to, UserProfile.PROFILE.GetPassword(), message, important, date, reminder.GetSendResponseListener(activity));
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);

        return reminder;
    }
    /** Insert a reminder into memory (local only). */
    public static Reminder LoadReminder(boolean silentLoad, int id, int user_id_from, int user_id_to, String message, boolean important, Date date, ReminderState state)
    {
        // Create a new reminder with the passed data.
        Reminder reminder = new Reminder(message, user_id_from, user_id_to, date);
        reminder.SetState(state);
        reminder.SetID(id);
        reminder.SetImportant(important);

        // Create a boolean to verify whether or not the active reminder is related to the created one.
        boolean selected = false;

        // Let's see if we have a Reminder with this ID already.
        Reminder check = UserProfile.PROFILE.GetReminder(id);
        if (check != null)
        {
            // Is the one checked equal to the active reminder
            if (UserProfile.PROFILE.GetActiveReminder() == check)
                selected = true;
            // Already have one, so we should remove that one...
            if (reminder.GetState() != check.GetState() && reminder.GetFrom() == UserProfile.PROFILE.GetUserID())
            {
                // Change in state, and we are the sender. Notify the user.
                reminder.ShowNotification(true, "Reminder Alert!", "Reminder has been marked: " + reminder.GetState().toString());
                reminder.SetUpToDate(false);
            }
            UserProfile.PROFILE.GetReminders().remove(check);
        }
        else
        {
            // New reminder has been added, make a notification
            if (!silentLoad)
                reminder.ShowNotification(true, "Reminder Alert!", reminder.GetMessage());
            reminder.SetUpToDate(false);
        }

        // Add the reminder to memory
        UserProfile.PROFILE.AddReminder(reminder);
        if (selected)
            UserProfile.PROFILE.SetActiveReminder(reminder);

        return reminder;
    }

    private int ID;
    private int User_ID_From;
    private int User_ID_To;
    private String FullName = null;
    private TextView Widget;
    private String Message;
    private Date Date;
    private boolean Important;

    private boolean UpToDate;
    private boolean Old;

    private ReminderState State;

    private String DateInProgress;
    private String DateComplete;

    private Reminder(String message, int user_id_from, int user_id_to, Date date)
    {
        Old = false;
        ID = 0;
        User_ID_From = user_id_from;
        User_ID_To = user_id_to;

        DateInProgress = null;
        DateComplete = null;

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
        UpToDate = true;
        State = ReminderState.NOT_STARTED;
    }

    public final int GetID() { return ID; }
    public final int GetFrom() { return User_ID_From; }
    public final int GetTo() { return User_ID_To; }
    public final String GetFullName() { return FullName; }
    public final String GetMessage() { return Message; }
    public final Date GetDate() { return Date; }
    public final boolean GetImportant() { return Important; }
    public final ReminderState GetState() { return State; }
    public final int GetStateOrdinal() { return State.ordinal(); }
    public final String GetDateInProgress() { return DateInProgress; }
    public final String GetDateComplete() { return DateComplete; }
    public final boolean IsUpToDate() { return UpToDate; }

    public void SetID(final int id) { ID = id; }
    public void SetImportant(final boolean value) { Important = value; }
    public void SetState(ReminderState state) { State = state; }
    public void SetWidget(TextView To) { Widget = To; }
    public void SetUpToDate(final boolean value) { UpToDate = value; }

    public void SetDateInProgress(final String date)
    {
        DateInProgress = date;
    }

    public void SetDateComplete(final String date)
    {
        DateComplete = date;
    }

    public TextView CreateWidget(final Activity activity, LinearLayout parent)
    {
        if (Widget != null)
            return Widget;

        final Reminder reminder = this;
        TextView view = new TextView(activity);

        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!IsUpToDate())
                    SetUpToDate(true);
                UserProfile.PROFILE.SetActiveReminder(reminder);
                UserProfile.PROFILE.RefreshReminderLayout();
            }
        });

        view.setMovementMethod(LinkMovementMethod.getInstance());
        Bitmap bState;
        if (GetState() == ReminderState.IN_PROGRESS)
            if (GetTo() == UserProfile.PROFILE.GetUserID())
                bState = BitmapFactory.decodeResource( activity.getResources(), R.drawable.running_96_blue );
            else
                bState = BitmapFactory.decodeResource( activity.getResources(), R.drawable.document_96_blue );
        else if (GetState() == ReminderState.COMPLETE)
            if (GetTo() == UserProfile.PROFILE.GetUserID())
                bState = BitmapFactory.decodeResource( activity.getResources(), R.drawable.running_96_green );
            else
                bState = BitmapFactory.decodeResource( activity.getResources(), R.drawable.document_96_green );
        else
            if (GetTo() == UserProfile.PROFILE.GetUserID())
                bState = BitmapFactory.decodeResource( activity.getResources(), R.drawable.running_96 );
            else
                bState = BitmapFactory.decodeResource( activity.getResources(), R.drawable.document_96 );
        Bitmap bDelete = BitmapFactory.decodeResource( activity.getResources(), R.drawable.delete_96 );
        Bitmap bMute;
        if (!UserProfile.PROFILE.IsIgnoring(GetID()))
            bMute = BitmapFactory.decodeResource( activity.getResources(), R.drawable.mute_96 );
        else
            bMute = BitmapFactory.decodeResource( activity.getResources(), R.drawable.mute_96_red );
        Bitmap bImportant = BitmapFactory.decodeResource( activity.getResources(), R.drawable.attention_48 );
        Bitmap bBack = BitmapFactory.decodeResource( activity.getResources(), R.drawable.back_arrow_48 );
        Bitmap bForward = BitmapFactory.decodeResource( activity.getResources(), R.drawable.forward_arrow_48 );
        Bitmap bUpdate = BitmapFactory.decodeResource( activity.getResources(), R.drawable.attention_48_blue);

        int color = Color.LTGRAY;
        if ((parent.getChildCount()) % 2 != 0)
            color = Color.argb(255, 176, 176, 176);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        String line1 =  "_ " + GetFullName() + System.getProperty("line.separator"); // Who
        String line2 = "Message: " + GetMessage() + System.getProperty("line.separator"); // What
        String line3 = "Deadline in: " + GetETA() + System.getProperty("line.separator"); // When
        String line4 = System.getProperty("line.separator");
        if (!IsUpToDate() && GetImportant())
            line4 = "_ _" + System.getProperty("line.separator");
        else if (GetImportant() || !IsUpToDate())
            line4 = "_" + System.getProperty("line.separator"); // warnings
        String line5 = "";
        if (UserProfile.PROFILE.GetActiveReminder() == this)
            line5 = "_ _ _"; // actions


        spannableStringBuilder.append(line1);
        spannableStringBuilder.append(line2);
        spannableStringBuilder.append(line3);
        spannableStringBuilder.append(line4);
        if (UserProfile.PROFILE.GetActiveReminder() == this)
            spannableStringBuilder.append(line5);

        spannableStringBuilder.setSpan(new RelativeSizeSpan(1.5f), 0, line1.length() - 1, 0);
        spannableStringBuilder.setSpan(new RelativeSizeSpan(1f), line1.length(), line1.length() + line2.length() + 1, 0);

        if (GetFrom() == UserProfile.PROFILE.GetUserID())
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bForward), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        else
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bBack), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        if (UserProfile.PROFILE.GetActiveReminder() == this)
        {
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bState), line1.length() + line2.length() + line3.length() + line4.length(), line1.length() + line2.length() + line3.length() + line4.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bDelete), line1.length() + line2.length() + line3.length() + line4.length() + 2, line1.length() + line2.length() + line3.length() + line4.length() + 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bMute), line1.length() + line2.length() + line3.length() + line4.length() + 4, line1.length() + line2.length() + line3.length() + line4.length() + 5, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        }

        //
        // If the reminder is marked as important, create an exclamation mark at the top right of the reminder.
        //
        if (GetImportant() && !IsUpToDate())
        {
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bImportant), line1.length() + line2.length() + line3.length(), line1.length() + line2.length() + line3.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bUpdate), line1.length() + line2.length() + line3.length() + 2, line1.length() + line2.length() + line3.length() + 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        else if (GetImportant())
        {
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bImportant), line1.length() + line2.length() + line3.length(), line1.length() + line2.length() + line3.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        else if (!IsUpToDate())
        {
            spannableStringBuilder.setSpan(new ImageSpan(view.getContext(), bUpdate), line1.length() + line2.length() + line3.length(), line1.length() + line2.length() + line3.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, line1.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), line1.length(), line1.length() + 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), line1.length() + line2.length(), line1.length() + line2.length() + 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        if (UserProfile.PROFILE.GetActiveReminder() == this)
        {
            final Reminder myReminder = this;

            ClickableSpan stateClick = new ClickableSpan() {
                @Override
                public void onClick(View view)
                {

                    if (!Network.IsConnected(UserAreaActivity.GetActivity()))
                        return;
                    //
                    if (UserProfile.PROFILE.GetUserID() == GetFrom())
                    {
                        final Dialog dialog = new Dialog(UserAreaActivity.GetActivity());
                        dialog.setTitle("Reminder Log");
                        dialog.setContentView(R.layout.dialog_reminder_log);
                        dialog.show();

                        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.reminder_log);

                        if (GetDateInProgress() == null && GetDateComplete() == null)
                        {
                            TextView tv = new TextView(dialog.getContext());
                            tv.setText("No activity.");
                            tv.setTextColor(Color.BLACK);
                            tv.setTextSize(18f);
                            layout.addView(tv);
                        }
                        else
                        {
                            try
                            {
                                final DateFormat formatter24Hour = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                                final DateFormat formatter12Hour = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");

                                if (GetDateInProgress() != null)
                                {
                                    Date date = formatter24Hour.parse(GetDateInProgress());
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);

                                    int hours = cal.get(Calendar.HOUR_OF_DAY);

                                    String suffix = "am";
                                    if (hours > 12)
                                        suffix = "pm";

                                    TextView tv = new TextView(dialog.getContext());
                                    tv.setText("'In Progress' at " + formatter12Hour.format(formatter12Hour.parse(GetDateInProgress())) + " " + suffix);
                                    tv.setTextColor(Color.BLACK);
                                    tv.setTextSize(16f);
                                    layout.addView(tv);
                                }
                                if (GetDateComplete() != null)
                                {
                                    Date date = formatter24Hour.parse(GetDateComplete());
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);

                                    int hours = cal.get(Calendar.HOUR_OF_DAY);

                                    String suffix = "am";
                                    if (hours > 12)
                                        suffix = "pm";

                                    TextView tv = new TextView(dialog.getContext());
                                    tv.setText("'Complete' at " + formatter12Hour.format(formatter12Hour.parse(GetDateComplete())) + " " + suffix);
                                    tv.setTextColor(Color.BLACK);
                                    tv.setTextSize(16f);
                                    layout.addView(tv);
                                }

                                if (GetDateComplete() != null && GetDateInProgress() != null)
                                {
                                    Button btn_thank = new Button(UserAreaActivity.GetActivity());
                                    btn_thank.setText("Send two coins!");
                                    layout.addView(btn_thank);

                                    if (UserProfile.PROFILE.GetPointsRemaining() < 2)
                                        btn_thank.setEnabled(false);

                                    btn_thank.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            //TODO: Lots and lots
                                            //Check if user has enough coins.
                                            //SendCoinsRequest
                                            //After receive response, if successful... delete local reminder
                                        }
                                    });
                                }
                            } catch (ParseException e)
                            {
                                e.printStackTrace();
                            }


                        }

                        return;
                    }

                    final Dialog dialog = new Dialog(UserAreaActivity.GetActivity());
                    dialog.setTitle("Select Reminder State");
                    dialog.setContentView(R.layout.dialog_reminder_state_picker);
                    dialog.show();

                    Button btn_not_started = (Button) dialog.findViewById(R.id.button_rsp_not_started);
                    Button btn_in_progress = (Button) dialog.findViewById(R.id.button_rsp_in_progress);
                    Button btn_complete = (Button) dialog.findViewById(R.id.button_rsp_complete);

                    if (GetDateInProgress() != null)
                        btn_not_started.setEnabled(false);
                    if (GetDateComplete() != null)
                        btn_in_progress.setEnabled(false);

                    btn_not_started.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            SetState(ReminderState.NOT_STARTED);
                            UserProfile.PROFILE.RefreshReminderLayout();
                            UserProfile.PROFILE.pushReminder(myReminder);
                            dialog.cancel();
                        }
                    });

                    btn_in_progress.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            SetState(ReminderState.IN_PROGRESS);
                            UserProfile.PROFILE.RefreshReminderLayout();
                            UserProfile.PROFILE.pushReminder(myReminder);
                            dialog.cancel();
                        }
                    });

                    btn_complete.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            SetState(ReminderState.COMPLETE);
                            UserProfile.PROFILE.RefreshReminderLayout();
                            UserProfile.PROFILE.pushReminder(myReminder);
                            dialog.cancel();
                        }
                    });

                    (dialog.findViewById(R.id.button_rsp_cancel)).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            dialog.cancel();
                        }
                    });
                }
            };
            ClickableSpan deleteClick = new ClickableSpan()
            {
                @Override
                public void onClick(View view)
                {
                    if (!Network.IsConnected(UserAreaActivity.GetActivity()))
                        return;
                    UserProfile.PROFILE.DeleteReminder(reminder);
                }
            };
            ClickableSpan muteClick = new ClickableSpan() {
                @Override
                public void onClick(View view)
                {
                    UserProfile.PROFILE.SetIgnoreReminder(GetID(), !UserProfile.PROFILE.IsIgnoring(GetID()));
                    UserProfile.PROFILE.RefreshReminderLayout();
                }
            };

            //line1.length() + line2.length() + line3.length() + line4.length(), line1.length() + line2.length() + line3.length() + line4.length() + 1, Spannable.SPAN_INCLUSIVE_INC
            //, line1.length() + line2.length() + line3.length() + line4.length() + 2, line1.length() + line2.length() + line3.length() + line4.length() + 3, Spannable.SPAN_INCLUSIV
            //line1.length() + line2.length() + line3.length() + line4.length() + 4, line1.length() + line2.length() + line3.length() + line4.length() + 5, Spannable.SPAN_INCLUSIVE_

            spannableStringBuilder.setSpan(stateClick, line1.length() + line2.length() + line3.length() + line4.length(), line1.length() + line2.length() + line3.length() + line4.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            spannableStringBuilder.setSpan(deleteClick, line1.length() + line2.length() + line3.length() + line4.length() + 2, line1.length() + line2.length() + line3.length() + line4.length() + 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            spannableStringBuilder.setSpan(muteClick, line1.length() + line2.length() + line3.length() + line4.length() + 4, line1.length() + line2.length() + line3.length() + line4.length() + 5, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        }

        view.setText(spannableStringBuilder);
        if (!GetImportant())
            view.setPadding(10, 12, 10, 12);
        else
            view.setPadding(10, 12, 10, 0);
        view.setBackgroundColor(color);



        Widget = view;
        return view;
    }
    public int[] GetTimeLeft()
    {
        int[] time = new int[3];

        Date now = new Date();

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

        time[0] = (int) days;
        time[1] = (int) hours;
        time[2] = (int) minutes;

        return time;
    }
    private String GetETA()
    {
        String eta = "";

        int[] timeLeft = GetTimeLeft();

        if (timeLeft[0] > 0)
            eta += timeLeft[0] + " days ";
        if (timeLeft[1] > 0)
            eta += timeLeft[1] + " hours ";
        if (timeLeft[2] > 0)
            eta += timeLeft[2] + " minutes ";

        return eta;
    }
    public boolean Remind()
    {
        if (UserProfile.PROFILE.GetUserID() != GetTo() || GetState() == ReminderState.COMPLETE)
            return false;

        //
        // TODO: Make times to notify customizable on the settings of the app.
        //

        final Reminder myReminder = this;

        int[] timeLeft = GetTimeLeft();
        if (timeLeft[0] <= 0 && timeLeft[1] <= 0 && timeLeft[2] <= 0)
        {
            //
            // Time is over.
            //

            final Dialog dialog = new Dialog(UserAreaActivity.GetActivity());
            dialog.setTitle("Reminder due; finalize state");
            dialog.setContentView(R.layout.dialog_reminder_finalize);
            dialog.show();

            TextView tv_who = (TextView) dialog.findViewById(R.id.textView_rf_who);
            TextView tv_message = (TextView) dialog.findViewById(R.id.textView_rf_message);

            tv_who.setText("Reminder from: " + myReminder.GetFullName());
            tv_message.setText("Message: " + myReminder.GetMessage());

            (dialog.findViewById(R.id.button_rf_not_started)).setOnClickListener(new View.OnClickListener()
            {


                @Override
                public void onClick(View view)
                {
                    SetState(ReminderState.NOT_STARTED);
                    UserProfile.PROFILE.RefreshReminderLayout();
                    UserProfile.PROFILE.pushReminder(myReminder);
                    dialog.cancel();
                }
            });

            (dialog.findViewById(R.id.button_rf_in_progress)).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    SetState(ReminderState.IN_PROGRESS);
                    UserProfile.PROFILE.RefreshReminderLayout();
                    UserProfile.PROFILE.pushReminder(myReminder);
                    dialog.cancel();
                }
            });

            (dialog.findViewById(R.id.button_rf_complete)).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    SetState(ReminderState.COMPLETE);
                    UserProfile.PROFILE.RefreshReminderLayout();
                    UserProfile.PROFILE.pushReminder(myReminder);
                    dialog.cancel();
                }
            });

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialogInterface)
                {
                    //TODO: Finish finalizing. Send changes to sender.
                }
            });
        }
        else if (timeLeft[0] == 1 && timeLeft[1] == 0 && timeLeft[2] == 0)
        {
            //
            // One day is left
            //

            ShowNotification(false, "Reminder from " + GetFullName(), "Due in 1 day.");
        }
        else if (timeLeft[0] == 0 && timeLeft[1] == 1 && timeLeft[2] == 0)
        {
            //
            // 1 hours left
            //

            ShowNotification(false, "Reminder from " + GetFullName(), "Due in 1 hour.");
        }

        return true;
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
                        //TODO: get reminder data from send, so we can add it locally.

                        JSONObject reminderJsonResponse = jsonResponse.getJSONObject("reminder");
                        int id = Integer.parseInt(reminderJsonResponse.getString("id"));
                        int from = Integer.parseInt(reminderJsonResponse.getString("id_from"));
                        int to = Integer.parseInt(reminderJsonResponse.getString("id_to"));
                        int important = Integer.parseInt(reminderJsonResponse.getString("important"));
                        String rMessage = reminderJsonResponse.getString("message");
                        String dateString = reminderJsonResponse.getString("date");
                        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                        Date date;
                        try
                        {
                            date = formatter.parse(dateString);
                            Reminder.LoadReminder(true, id, from, to, rMessage, (important > 0) ? true : false, date, ReminderState.NOT_STARTED);
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }

                        Log.d("INFO", "Reminder ID: " + id);

                        PullScheduler.Call();
                        if (activity != null)
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

    public Response.Listener<String> GetDeleteResponseListener(final Activity activity)
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
                        UserProfile.PROFILE.Pull(activity);
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

    public Response.Listener<String> GetUpdateResponseListener(final Activity activity)
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
                        UserProfile.PROFILE.Pull(activity);
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

    public void ShowNotification(boolean vibrate, String title, String message)
    {
        if (UserProfile.PROFILE.IsIgnoring(GetID()))
            return;

        PendingIntent pi = PendingIntent.getActivity(UserAreaActivity.GetActivity(), 0, new Intent(UserAreaActivity.GetActivity().getBaseContext(), UserAreaActivity.class), 0);
        //Resources r = UserAreaActivity.GetActivity().getResources();
        Notification notification = new NotificationCompat.Builder(UserAreaActivity.GetActivity())
                .setTicker(title)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        if (vibrate)
        {
            NotificationManager notificationManager = (NotificationManager) UserAreaActivity.GetActivity().getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
            Vibrator v = (Vibrator) UserAreaActivity.GetActivity().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }

    }

    @Override
    public int compareTo(Reminder to)
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateString1 = formatter.format(GetDate());
        String dateString2 = formatter.format(to.GetDate());

        return dateString1.compareTo(dateString2);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Reminder)
        {
            Reminder r = (Reminder) o;
            if (r.GetID() == GetID())
                return true;
            else
                return false;
        }
        else
            return false;
    }

    @Override
    public String toString()
    {
        return super.toString();
    }

    public String[] toArray()
    {
        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        String[] array = {
                String.valueOf(GetID()),
                String.valueOf(GetFrom()),
                String.valueOf(GetTo()),
                GetMessage(),
                formatter.format(GetDate()),
                String.valueOf((GetImportant() == true) ? 1 : 0),
                String.valueOf(GetState().ordinal())
        };
        return array;
    }
}

enum ReminderState
{
    NOT_STARTED, IN_PROGRESS, COMPLETE;

    @Override
    public String toString()
    {
        if (ordinal() == 0)
            return "Not Started";
        else if (ordinal() == 1)
            return "In Progress";
        else if (ordinal() == 2)
            return "Completed";
        else
            return super.toString();
    }
}