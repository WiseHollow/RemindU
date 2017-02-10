package net.johnbrooks.remindu.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.util.Reminder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CreateReminderActivity extends AppCompatActivity
{
    public ProgressBar progressBar;

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

        progressBar = (ProgressBar) findViewById(R.id.create_reminder_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        //
        // Prepare back button
        //

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //

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
                final TimePickerDialog tpDialog = new TimePickerDialog(CreateReminderActivity.this, 0, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hours, int minutes)
                    {
                        calendar.set(Calendar.HOUR_OF_DAY, hours);
                        calendar.set(Calendar.MINUTE, minutes);
                        calendar.set(Calendar.SECOND, 0);
                        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
                        String suffix = "am";
                        if (hours >= 12)
                            suffix = "pm";
                        tv_time.setText("Time Due: " + dateFormat.format(calendar.getTime()) + suffix);
                    }
                },calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                tpDialog.show();
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
                cView.setDate(calendar.getTimeInMillis());
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
                progressBar.setVisibility(View.VISIBLE);
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
                b_send.setEnabled(false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}