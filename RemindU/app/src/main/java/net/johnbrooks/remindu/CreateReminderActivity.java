package net.johnbrooks.remindu;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateReminderActivity extends AppCompatActivity
{
    private TextView tv_date;
    private TextView tv_time;
    private TextView tv_recipient;
    private EditText et_message;
    private Switch s_important;

    private Button b_time;
    private Button b_date;
    private Button b_send;

    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder);

        tv_date = (TextView) findViewById(R.id.textView_cnr_date);
        tv_time = (TextView) findViewById(R.id.textView_cnr_time);
        tv_recipient = (TextView) findViewById(R.id.textView_cnr_recipient);
        et_message = (EditText) findViewById(R.id.editText_cnr_message);
        s_important = (Switch) findViewById(R.id.switch_cnr_important);

        b_time = (Button) findViewById(R.id.button_cnr_pick_time);
        b_date = (Button) findViewById(R.id.button_cnr_pick_date);
        b_send = (Button) findViewById(R.id.button_cnr_send);

        tv_recipient.setText("Recipient: " + getIntent().getStringExtra("user_to_fullname"));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        tv_date.setText("Date Due: " + dateFormat.format(calendar.getTime()));

        DateFormat timeFormat = new SimpleDateFormat("hh:mm");
        String suffix = "am";
        if (calendar.get(Calendar.HOUR_OF_DAY) > 12)
            suffix = "pm";
        tv_time.setText("Time Due: " + timeFormat.format(calendar.getTime()) + suffix);

        b_time.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final Dialog dialog = new Dialog(CreateReminderActivity.this);
                dialog.setTitle("Pick Reminder Time Due");
                dialog.setContentView(R.layout.dialog_time_picker);
                dialog.show();

                TimePicker tp = (TimePicker) dialog.findViewById(R.id.timePicker_cnr);
                Button finished = (Button) dialog.findViewById(R.id.button_cnr_time_picker);

                Calendar now = Calendar.getInstance();
                calendar.set(Calendar.HOUR, now.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, 0);

                finished.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        dialog.cancel();
                    }
                });
                tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener()
                {
                    @Override
                    public void onTimeChanged(TimePicker timePicker, int hours, int minutes)
                    {
                        calendar.set(Calendar.HOUR, hours);
                        calendar.set(Calendar.MINUTE, minutes);
                        calendar.set(Calendar.SECOND, 0);
                        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
                        String suffix = "am";
                        if (hours > 12)
                            suffix = "pm";
                        tv_time.setText("Time Due: " + dateFormat.format(calendar.getTime()) + suffix);
                    }
                });
            }
        });

        b_date.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final Dialog dialog = new Dialog(CreateReminderActivity.this);
                dialog.setTitle("Pick Reminder Due Date");
                dialog.setContentView(R.layout.dialog_date_picker);
                dialog.show();

                CalendarView cView = (CalendarView) dialog.findViewById(R.id.calendarView_cnr);
                cView.setOnDateChangeListener(new CalendarView.OnDateChangeListener()
                {
                    @Override
                    public void onSelectedDayChange(CalendarView calendarView, int year, int month, int day)
                    {
                        // pass back info to date
                        calendar.set(year, month, day);
                        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                        tv_date.setText("Date Due: " + dateFormat.format(calendar.getTime()));
                        dialog.cancel();
                    }
                });
            }
        });

        b_send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String message = et_message.getText().toString();
                boolean important = s_important.isChecked();

                int user_id_to = getIntent().getIntExtra("user_id_to", 0);
                if (user_id_to == 0)
                {
                    Log.d("SEVERE", "Cannot find user_id_to for creating reminder.");
                    finish();
                    return;
                }

                Reminder.CreateReminder(user_id_to, message, important, calendar.getTime(), CreateReminderActivity.this);
            }
        });
    }
}
