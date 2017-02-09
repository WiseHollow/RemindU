package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.CreateReminderActivity;
import net.johnbrooks.remindu.activities.ReminderListActivity;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.requests.SendReputationRequest;
import net.johnbrooks.remindu.requests.SendReminderRequest;
import net.johnbrooks.remindu.schedulers.MasterScheduler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by John on 11/28/2016.
 */

public class Reminder implements Comparable<Reminder>
{
    /** Upload the request to the user's online records. */
    public static Reminder CreateReminder(int user_id_to, String message, boolean important, Date date, CreateReminderActivity activity)
    {
        Reminder reminder = new Reminder(message, UserProfile.PROFILE.GetUserID(), user_id_to, date);
        reminder.SetImportant(important);

        SendReminderRequest.SendRequest(activity, reminder);

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
                reminder.ShowNotification(true, "Activity Update: ", "Task marked as: " + reminder.GetState().toString().toLowerCase());
                reminder.SetUpToDate(false);
            }
            UserProfile.PROFILE.GetReminders().remove(check);
        }
        else
        {
            // New reminder has been added, make a notification
            if (!silentLoad)
            {
                reminder.ShowNotification(true, "New activity from: " + reminder.GetFullName(), reminder.GetMessage());

            }
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
    public final boolean IsOld() { return Old; }

    public void SetOld(boolean value) { Old = value; }
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

    public String GetStateSince(final ReminderState state)
    {
        if (state == null)
            return "";

        if (state == ReminderState.IN_PROGRESS && DateInProgress != null)
        {
            return DateInProgress;
        }
        else if (state == ReminderState.COMPLETE && DateComplete != null)
        {
            return DateComplete;
        }

        return "";
    }

    public int GetStateColor()
    {
        long now = (new Date()).getTime();
        long due = Date.getTime();

        int percent = 100 - (int) (now / due * 100);

        if (percent > 75)
            return Color.parseColor("#a8ffaf");
        else if (percent > 50)
            return Color.parseColor("#faffa8");
        else
            return Color.parseColor("#ffb0a8");
    }

    public LinearLayout CreateWidget(final ReminderListActivity activity)
    {
        final Reminder reminder = this;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1.0f
                );
        LinearLayout parent = new LinearLayout(activity);
        parent.setLayoutParams(params);
        parent.setOrientation(LinearLayout.VERTICAL);

        //
        // Set focus action
        //
        parent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!IsUpToDate())
                    SetUpToDate(true);
                UserProfile.PROFILE.SetActiveReminder(reminder);
                activity.RefreshReminderLayout();
                //UserProfile.PROFILE.RefreshReminderLayout();
            }
        });

        //
        // Contents of upper portion
        //

        LinearLayout layout = new LinearLayout(activity);
        layout.setLayoutParams(params);
        layout.setPadding(15, 0, 5, 5);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        // Image on left
        ImageView imageView = new ImageView(activity);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 8, 8, 8);
        if (!IsUpToDate())
            imageView.setLayoutParams(new GridView.LayoutParams(10, 250));
        else
            imageView.setLayoutParams(new GridView.LayoutParams(5, 250));
        if (GetImportant())
        {
            imageView.setBackgroundColor(Color.parseColor("#AA3939"));
            //layout.setBackgroundColor(Color.parseColor("#FFDEDE"));
        }
        else
        {
            imageView.setBackgroundColor(Color.parseColor("#80cfa9"));
            //layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        layout.addView(imageView);

        //Content inside
        TextView info = new TextView(activity);
        info.setPadding(10, 0, 10, 0);

        SpannableStringBuilder sString = new SpannableStringBuilder();
        if (GetFrom() == UserProfile.PROFILE.GetUserID())
        {
            sString.append("To: " + GetFullName());
            sString.setSpan(new StyleSpan(Typeface.BOLD), 0, 3, 0);
        }
        else
        {
            sString.append("From: " + GetFullName());
            sString.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, 0);
        }
        int length = sString.length();

        sString.append("\nActivity: " + GetMessage());
        sString.setSpan(new StyleSpan(Typeface.BOLD), length, length + 10, 0);
        length = sString.length();
        sString.append("\n\n" + "Time Left: " + GetETA());
        sString.setSpan(new StyleSpan(Typeface.BOLD), length, length + 10, 0);
        //sString.append("\n\n" + "_ " + GetETA());

        //Drawable d = ContextCompat.getDrawable(activity, R.drawable.clock_48);
        //sString.setSpan(new ImageSpan(d), length + 2, length + 3, 0);

        //TODO: Replace text with clock image.

        info.setText(sString);
        layout.addView(info);

        parent.addView(layout);

        //
        // Contents of lower portion
        //

        if (UserProfile.PROFILE.GetActiveReminder() == this)
        {
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams
                    (
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT, 0.3f
                    );

            LinearLayout btnLayout = new LinearLayout(activity);
            btnLayout.setOrientation(LinearLayout.HORIZONTAL);
            btnLayout.setLayoutParams(params);
            parent.addView(btnLayout);

            Button logBtn = new Button(activity);
            logBtn.setLayoutParams(btnParams);
            if (GetFrom() == UserProfile.PROFILE.GetUserID())
                logBtn.setText("See Log");
            else
                logBtn.setText("Mark as");
            logBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(activity.getApplicationContext(), GetLogButtonResourceID()), null, null, null);
            btnLayout.addView(logBtn);
            logBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ClickLogButton(activity, reminder);
                }
            });

            Button actionBtn = new Button(activity);
            actionBtn.setLayoutParams(btnParams);

            if (GetFrom() == UserProfile.PROFILE.GetUserID() && GetState() == ReminderState.COMPLETE)
                actionBtn.setText("Finish");
            else
                actionBtn.setText("Remove");
            actionBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(activity.getApplicationContext(), GetActionButtonResourceID()), null, null, null);
            btnLayout.addView(actionBtn);
            actionBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ClickRemoveButton(activity, reminder);
                }
            });

            Button muteBtn = new Button(activity);
            muteBtn.setLayoutParams(btnParams);
            muteBtn.setText("Mute");
            muteBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(activity.getApplicationContext(), GetMuteButtonResourceID()), null, null, null);
            btnLayout.addView(muteBtn);
            muteBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ClickMuteButton(activity, reminder);
                }
            });
        }

        return parent;
    }

    public void ClickLogButton(final Activity activity, final Reminder reminder)
    {
        if (!Network.IsConnected(activity))
            return;
        //
        if (UserProfile.PROFILE.GetUserID() == GetFrom())
        {
            final Dialog dialog = new Dialog(activity);
            dialog.setTitle("Activity Log");
            dialog.setContentView(R.layout.dialog_reminder_log);
            dialog.show();

            Button button_close = (Button) dialog.findViewById(R.id.log_entry_button_okay);
            button_close.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    dialog.cancel();
                }
            });

            dialog.findViewById(R.id.log_entry_progress_bar).setBackgroundColor(GetStateColor());
            ((TextView) dialog.findViewById(R.id.log_entry_progress_bar_text)).setText("Time left: " + GetETA());

            if (State == ReminderState.NOT_STARTED)
                dialog.findViewById(R.id.log_entry_image).setBackgroundResource(R.drawable.running_48);
            else if (State == ReminderState.IN_PROGRESS)
                dialog.findViewById(R.id.log_entry_image).setBackgroundResource(R.drawable.running_48_blue);
            else if (State == ReminderState.COMPLETE)
                dialog.findViewById(R.id.log_entry_image).setBackgroundResource(R.drawable.running_48_green);

            String state = GetState().name().replace("_", " ").toLowerCase();
            state = state.replace(String.valueOf(state.charAt(0)), String.valueOf(state.charAt(0)).toUpperCase());
            if (state.contains(" "))
                state = state.replace(String.valueOf(state.charAt(state.indexOf(" ") + 1)), String.valueOf(state.charAt(state.indexOf(" ") + 1)).toUpperCase());

            ((TextView) dialog.findViewById(R.id.log_entry_state)).setText(state);

            if (State == ReminderState.NOT_STARTED)
                ((TextView) dialog.findViewById(R.id.log_entry_since)).setText("");
            else
                ((TextView) dialog.findViewById(R.id.log_entry_since)).setText("Since: " + GetStateSince(State));


            return;
        }

        final Dialog dialog = new Dialog(activity);
        dialog.setTitle("Select Activity State");
        dialog.setContentView(R.layout.dialog_reminder_state_picker);
        dialog.show();

        Button btn_in_progress = (Button) dialog.findViewById(R.id.button_rsp_in_progress);
        Button btn_complete = (Button) dialog.findViewById(R.id.button_rsp_complete);

        if (GetDateInProgress() != null)
        {
            btn_in_progress.setEnabled(false);
        }
        if (GetDateComplete() != null)
        {
            btn_in_progress.setEnabled(false);
            btn_complete.setEnabled(false);
        }

        btn_in_progress.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SetState(ReminderState.IN_PROGRESS);
                UserProfile.PROFILE.RefreshReminderLayout();
                UserProfile.PROFILE.pushReminder(reminder);
                dialog.cancel();

                if (ReminderListActivity.GetActivity() != null)
                    ReminderListActivity.GetActivity().RefreshReminderLayout();
            }
        });

        btn_complete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SetState(ReminderState.COMPLETE);
                UserProfile.PROFILE.RefreshReminderLayout();
                UserProfile.PROFILE.pushReminder(reminder);
                dialog.cancel();

                if (ReminderListActivity.GetActivity() != null)
                    ReminderListActivity.GetActivity().RefreshReminderLayout();
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

    private void ClickRemoveButton(final ReminderListActivity activity, final Reminder reminder)
    {
        if (!Network.IsConnected(activity))
            return;
        //TODO: If completed task, give send coin dialog. or not, just delete.

        if (reminder.GetState() != ReminderState.COMPLETE || reminder.GetFrom() != UserProfile.PROFILE.GetUserID())
            UserProfile.PROFILE.DeleteReminder(reminder);
        else
        {
            final Dialog dialog = new Dialog(activity);
            dialog.setTitle("Send User Reputation");
            dialog.setContentView(R.layout.dialog_send_coins);
            dialog.show();

            final TextView tv_recipient = (TextView) dialog.findViewById(R.id.textView_sc_recipient);
            final TextView tv_coins = (TextView) dialog.findViewById(R.id.textView_sc_coins);
            final Button button_skip = (Button) dialog.findViewById(R.id.button_sc_skip);
            final Button button_send_1 = (Button) dialog.findViewById(R.id.button_sc_send_1);
            final Button button_send_5 = (Button) dialog.findViewById(R.id.button_sc_send_5);
            final Button button_send_10 = (Button) dialog.findViewById(R.id.button_sc_send_10);

            tv_recipient.setText("Recipient: " + reminder.GetFullName());
            tv_coins.setText("Reputation: " + UserProfile.PROFILE.GetCoins());

            button_skip.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    UserProfile.PROFILE.DeleteReminder(reminder);
                    dialog.cancel();

                    if (ReminderListActivity.GetActivity() != null)
                        ReminderListActivity.GetActivity().RefreshReminderLayout();
                }
            });

            button_send_1.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    SendReputationRequest.SendRequest(activity, reminder.GetTo(), 1);
                    UserProfile.PROFILE.DeleteReminder(reminder);
                    dialog.cancel();

                    if (ReminderListActivity.GetActivity() != null)
                        ReminderListActivity.GetActivity().RefreshReminderLayout();
                }
            });

            button_send_5.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    SendReputationRequest.SendRequest(activity, reminder.GetTo(), 2);
                    UserProfile.PROFILE.DeleteReminder(reminder);
                    dialog.cancel();

                    if (ReminderListActivity.GetActivity() != null)
                        ReminderListActivity.GetActivity().RefreshReminderLayout();
                }
            });

            button_send_10.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    SendReputationRequest.SendRequest(activity, reminder.GetTo(), 3);
                    UserProfile.PROFILE.DeleteReminder(reminder);
                    dialog.cancel();

                    if (ReminderListActivity.GetActivity() != null)
                        ReminderListActivity.GetActivity().RefreshReminderLayout();
                }
            });
        }

        if (ReminderListActivity.GetActivity() != null)
            ReminderListActivity.GetActivity().RefreshReminderLayout();
    }

    private void ClickMuteButton(final ReminderListActivity activity, final Reminder reminder)
    {
        UserProfile.PROFILE.SetIgnoreReminder(GetID(), !UserProfile.PROFILE.IsIgnoring(GetID()));
        activity.RefreshReminderLayout();
    }

    private int GetLogButtonResourceID()
    {
        if (GetState() == ReminderState.IN_PROGRESS)
        if (GetTo() == UserProfile.PROFILE.GetUserID())
            return R.drawable.running_48_blue;
        else
            return R.drawable.document_48_blue;
        else if (GetState() == ReminderState.COMPLETE)
        if (GetTo() == UserProfile.PROFILE.GetUserID())
            return R.drawable.running_48_green;
        else
            return R.drawable.document_48_green;
    else
    if (GetTo() == UserProfile.PROFILE.GetUserID())
        return R.drawable.running_48;
    else
        return R.drawable.document_48;
    }

    private int GetActionButtonResourceID()
    {
        if (GetState() != ReminderState.COMPLETE || GetFrom() != UserProfile.PROFILE.GetUserID())
        {
            return R.drawable.delete_48;
        }
        else
        {
            return R.drawable.coins_48_blue;
        }
    }

    private int GetMuteButtonResourceID()
    {
        if (!UserProfile.PROFILE.IsIgnoring(GetID()))
            return R.drawable.mute_48;
        else
            return R.drawable.mute_48_red;
    }

    public int[] GetTimeRemaining(Date compare)
    {
        int[] time = new int[3];

        long remaining = compare.getTime() - (new Date()).getTime();
        remaining = remaining / 1000;

        if (remaining != 0)
        {
            time[0] = (int) (remaining / (24 * 60 * 60));
            if (time[0] != 0)
                remaining = remaining % (24 * 60 * 60);
        }

        if (remaining != 0)
        {
            time[1] = (int) (remaining / (60 * 60));
            if (time[1] != 0)
                remaining = remaining % (60 * 60);
        }

        if (remaining != 0)
        {
            time[2] = (int) (remaining / (60));
        }

        return time;
    }

    public int[] GetTimeSince(Date compare)
    {
        int[] time = new int[3];

        long remaining = (new Date()).getTime() - compare.getTime();
        remaining = remaining / 1000;

        if (remaining != 0)
        {
            time[0] = (int) (remaining / (24 * 60 * 60));
            if (time[0] != 0)
                remaining = remaining % (24 * 60 * 60);
        }

        if (remaining != 0)
        {
            time[1] = (int) (remaining / (60 * 60));
            if (time[1] != 0)
                remaining = remaining % (60 * 60);
        }

        if (remaining != 0)
        {
            time[2] = (int) (remaining / (60));
        }

        return time;
    }

    private String GetETA()
    {
        String eta = "";

        int[] timeLeft = GetTimeRemaining(Date);

        if (timeLeft[0] > 0)
            eta += timeLeft[0] + " days ";
        if (timeLeft[1] > 0)
            eta += timeLeft[1] + " hours ";
        if (timeLeft[2] > 0)
            eta += timeLeft[2] + " minutes ";

        if (eta.equalsIgnoreCase(""))
            eta = "Past Due";

        return eta;
    }

    public String GetRoughETA(Date date)
    {
        int[] timeLeft = GetTimeRemaining(date);

        if (timeLeft[0] > 0)
        {
            if (timeLeft[0] == 1)
                return "A day ago";
            else
                return timeLeft[0] + " days ago";
        }
        else if (timeLeft[1] > 0)
        {
            if (timeLeft[0] == 1)
                return "An hour ago";
            else
                return timeLeft[0] + " hours ago";
        }
        else if (timeLeft[2] > 0)
        {
            if (timeLeft[0] == 1)
                return "A minute ago";
            else
                return timeLeft[0] + " minutes ago";
        }
        else
        {
            return "Now";
        }
    }

    public String GetRoughTimeSince(Date date)
    {
        int[] timeLeft = GetTimeSince(date);

        if (timeLeft[0] > 0)
        {
            if (timeLeft[0] == 1)
                return "A day ago";
            else
                return timeLeft[0] + " days ago";
        }
        else if (timeLeft[1] > 0)
        {
            if (timeLeft[1] == 1)
                return "An hour ago";
            else
                return timeLeft[1] + " hours ago";
        }
        else if (timeLeft[2] > 0)
        {
            if (timeLeft[2] == 1)
                return "A minute ago";
            else
                return timeLeft[2] + " minutes ago";
        }
        else
        {
            return "Now";
        }
    }

    public boolean ProcessReminderNotifications()
    {
        if (MasterScheduler.GetInstance() == null || (MasterScheduler.GetInstance().GetActivity() == null && MasterScheduler.GetInstance().GetService() == null))
            return false;

        if (GetState() == ReminderState.COMPLETE)
            return false;
        //
        // TODO: Make times to notify customizable on the settings of the app.
        //

        int[] timeLeft = GetTimeRemaining(Date);
        if (timeLeft[0] == 0 && timeLeft[1] == 0 && timeLeft[2] == 0)
        {
            //
            // Time is over.
            //

            ShowNotification(true, "Activity from " + GetFullName(), "Passed deadline.");
        }
        else if (timeLeft[0] == 1 && timeLeft[1] == 0 && timeLeft[2] == 0)
        {
            //
            // One day is left
            //

            ShowNotification(true, "Activity from " + GetFullName(), "Due in 1 day.");
        }
        else if (timeLeft[0] == 0 && timeLeft[1] == 1 && timeLeft[2] == 0)
        {
            //
            // 1 hours left
            //

            ShowNotification(true, "Activity from " + GetFullName(), "Due in 1 hour.");
        }

        return true;
    }
    public boolean ProcessReminderDialogs()
    {
        if (UserProfile.PROFILE.GetUserID() != GetTo() || GetState() == ReminderState.COMPLETE)
            return false;

        //
        // TODO: Make times to notify customizable on the settings of the app.
        //

        final Reminder myReminder = this;

        int[] timeLeft = GetTimeRemaining(Date);
        if (timeLeft[0] == 0 && timeLeft[1] == 0 && timeLeft[2] == 0)
        {
            //
            // Time is over.
            //

            final Dialog dialog = new Dialog(UserAreaActivity.GetActivity());
            dialog.setTitle("Activity due; finalize state");
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

        return true;
    }

    private NotificationCompat.Builder GetNotification(String title, String message)
    {
        NotificationCompat.Builder mBuilder;
        Intent intent;
        TaskStackBuilder stackBuilder;

        if (MasterScheduler.GetInstance() == null) { return null; }

        mBuilder = new NotificationCompat.Builder(MasterScheduler.GetInstance().GetContextWrapper())
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(title)
                .setContentText(message);
        intent = new Intent(MasterScheduler.GetInstance().GetContextWrapper(), ReminderListActivity.class);
        stackBuilder = TaskStackBuilder.create(MasterScheduler.GetInstance().GetContextWrapper());

        if (GetFrom() == UserProfile.PROFILE.GetUserID())
            intent.putExtra("contactID", GetTo());
        else
            intent.putExtra("contactID", GetFrom());

        stackBuilder.addParentStack(ReminderListActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(GetID(), PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        return mBuilder;
    }

    public void ShowNotification(boolean vibrate, String title, String message)
    {
        if (MasterScheduler.GetInstance() == null || MasterScheduler.GetInstance().GetContextWrapper() == null) { return; }

        NotificationCompat.Builder notification = GetNotification(title, message);
        NotificationManager mNotificationManager = (NotificationManager) MasterScheduler.GetInstance().GetContextWrapper().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, notification.build());
        if (!vibrate) { return; }

        AudioManager am = (AudioManager) MasterScheduler.GetInstance().GetContextWrapper().getSystemService(Context.AUDIO_SERVICE);

        if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
        {
            try
            {
                Vibrator v = (Vibrator) MasterScheduler.GetInstance().GetContextWrapper().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
        {
            try
            {
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(MasterScheduler.GetInstance().GetContextWrapper().getApplicationContext(), alarmSound);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int compareTo(Reminder to)
    {
        if (UserProfile.PROFILE.sortRemindersByDueDate)
        {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateString1 = formatter.format(GetDate());
            String dateString2 = formatter.format(to.GetDate());

            return dateString1.compareTo(dateString2);
        }
        else
        {
            String s1 = "";

            if (GetDateComplete() != null)
                s1 = GetDateComplete();
            else if (GetDateInProgress() != null)
                s1 = GetDateInProgress();

            String s2 = "";

            if (to.GetDateComplete() != null)
                s2 = to.GetDateComplete();
            else if (to.GetDateInProgress() != null)
                s2 = to.GetDateInProgress();

            return s2.compareTo(s1);
        }
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
                String.valueOf(GetState().ordinal()),
                GetDateInProgress(),
                GetDateComplete()
        };
        return array;
    }
    public enum ReminderState
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
}
