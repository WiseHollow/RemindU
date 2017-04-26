package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.exceptions.ReminderNotFoundException;
import net.johnbrooks.remindu.fragments.FeedFragment;
import net.johnbrooks.remindu.requests.UpdateReminderLikeRequest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        else if (state == Reminder.ReminderState.NOT_STARTED)
            return reminder.GetDateCreated();
        else
            return null;
    }

    public final void SetLiked(boolean liked)
    {
        this.liked = liked;
    }

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

    public LinearLayout CreateWidget(final Activity activity)
    {
        final ReminderFlag flag = this;
        final LinearLayout widget = (LinearLayout) UserAreaActivity.GetActivity().GetLayoutInflater().inflate(R.layout.widget_reminder_in_feed, null);

        // listener for popping up the toolbar.
        View.OnClickListener clickFlagListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //When a flag is clicked
                UserProfile.PROFILE.SetActiveReminderFlag(flag);
                if (FeedFragment.GetInstance() != null)
                    FeedFragment.GetInstance().PopulateActivity();
            }
        };

        // listener that will close the toolbar.

        LinearLayout layoutDesc = (LinearLayout) widget.findViewById(R.id.feed_element_layout_desc);
        LinearLayout layoutState = (LinearLayout) widget.findViewById(R.id.feed_element_state_desc);

        layoutDesc.setOnClickListener(clickFlagListener);
        layoutState.setOnClickListener(clickFlagListener);

        //final View view = widget.findViewById(R.id.feed_element_layout);

        final ImageView iv_avatar = (ImageView) widget.findViewById(R.id.feed_element_avatar);
        final TextView tv_fullName = (TextView) widget.findViewById(R.id.feed_element_fullName);
        final TextView tv_state = (TextView) widget.findViewById(R.id.feed_element_state);
        final TextView tv_activityInfo = (TextView) widget.findViewById(R.id.feed_element_activityInfo);
        final TextView tv_time = (TextView) widget.findViewById(R.id.feed_element_time);
        final ImageView iv_like = (ImageView) widget.findViewById(R.id.feed_element_like);
        final ImageView iv_state = (ImageView) widget.findViewById(R.id.feed_element_image_state);

        if (flag.GetReminder().GetTo() != UserProfile.PROFILE.GetUserID())
            widget.findViewById(R.id.feed_element_available).setVisibility(View.INVISIBLE);

        if (flag.GetState() == Reminder.ReminderState.NOT_STARTED)
            iv_state.setImageResource(R.drawable.create_new_48);
        else if (flag.GetState() == Reminder.ReminderState.IN_PROGRESS)
            iv_state.setImageResource(R.drawable.in_progress_48);
        else
            iv_state.setImageResource(R.drawable.activity_complete_48);

        if (flag.IsLiked())
            iv_like.setImageResource(R.drawable.like_it_filled_48);
        else
            iv_like.setImageResource(R.drawable.like_it_48);
        iv_like.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                flag.SetLiked(!flag.IsLiked());
                if (flag.IsLiked())
                    iv_like.setImageResource(R.drawable.like_it_blue_48);
                else
                    iv_like.setImageResource(R.drawable.like_it_48);
                UserProfile.PROFILE.SaveReminderFlagsToFile();
                UpdateReminderLikeRequest.SendRequest(flag);
            }
        });

        ContactProfile cp;

        String avatar_id;
        String full_name;

        if (flag.GetState() == Reminder.ReminderState.NOT_STARTED)
            cp = ContactProfile.GetProfile(flag.GetReminder().GetFrom());
        else
            cp = ContactProfile.GetProfile(flag.GetReminder().GetTo());

        if (cp == null)
        {
            avatar_id = UserProfile.PROFILE.GetAvatarID();
            full_name = UserProfile.PROFILE.GetFullName();

            iv_avatar.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    UserProfile.PROFILE.CreatePreviewDialog(UserAreaActivity.GetActivity()).show();
                }
            });
        }
        else
        {
            avatar_id = cp.GetAvatarID();
            full_name = cp.GetFullName();

            final ContactProfile fCP = cp;

            iv_avatar.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    fCP.CreatePreviewDialog(UserAreaActivity.GetActivity()).show();
                }
            });
        }


        if (avatar_id == null || full_name == null)
        {
            Log.d("SEVERE", "FeedFragment cannot locate avatar_id and full_name!");
            return null;
        }

        iv_avatar.setBackground(AvatarImageUtil.GetAvatar(avatar_id));

        tv_fullName.setText(full_name);

        String state = flag.GetState().toString();
        state = state.substring(0, 1).toUpperCase() + state.substring(1);

        tv_state.setText(state);
        tv_activityInfo.setText(flag.GetReminder().GetMessage());

        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        Date reminderDateToCompare;
        String timeLeft;

        try
        {
            reminderDateToCompare = formatter.parse(flag.GetDateOfFlag());
            timeLeft = flag.GetReminder().GetRoughTimeSince(reminderDateToCompare);
        } catch (ParseException e)
        {
            e.printStackTrace();
            timeLeft = "error";
        }

        tv_time.setText(timeLeft);

        if (UserProfile.PROFILE.GetActiveReminderFlag() == flag)
        {
            final LinearLayout controlBar = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.widget_reminder_control_bar, null);

            final ImageView iv_controlBar_1 = (ImageView) controlBar.findViewById(R.id.reminder_cp_button_1);
            final ImageView iv_controlBar_2 = (ImageView) controlBar.findViewById(R.id.reminder_cp_button_2);
            final ImageView iv_controlBar_3 = (ImageView) controlBar.findViewById(R.id.reminder_cp_button_3);

            iv_controlBar_1.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    flag.GetReminder().ClickLogButton(activity);
                }
            });

            iv_controlBar_2.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    flag.GetReminder().ClickRemoveButton(activity);
                }
            });

            iv_controlBar_3.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    flag.GetReminder().ClickMuteButton();
                    if (FeedFragment.GetInstance() != null)
                        FeedFragment.GetInstance().RefreshReminderLayout();
                }
            });

            iv_controlBar_1.setImageResource(flag.GetReminder().GetLogButtonResourceID());
            iv_controlBar_2.setImageResource(flag.GetReminder().GetActionButtonResourceID());
            iv_controlBar_3.setImageResource(flag.GetReminder().GetMuteButtonResourceID());

            widget.addView(controlBar);
        }

        return widget;
    }
}
