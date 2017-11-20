package com.emotilog.app.emotilog;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.support.v7.app.ActionBar;

public class SettingsActivity extends AppCompatActivity {
    private MyDatabaseHelper dbHelper;
    String TAG= "RemindMe";
    LocalData localData;

    SwitchCompat reminderSwitch;
    TextView tvTime;

    LinearLayout ll_set_time;
    int hour, min;

    ClipboardManager myClipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#7f8c8d")));
        this.getWindow().setStatusBarColor(Color.parseColor("#657070"));

        dbHelper = new MyDatabaseHelper(this,"entrys_db",null,1);
        localData=new LocalData(getApplicationContext());

        myClipboard= (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        ll_set_time=(LinearLayout) findViewById(R.id.ll_set_time);

        tvTime= (TextView) findViewById(R.id.tv_reminder_time_desc);

        reminderSwitch=(SwitchCompat) findViewById(R.id.timerSwitch);

        hour= localData.get_hour();
        min=localData.get_min();


        tvTime.setText(getFormatedTime(hour, min));
        reminderSwitch.setChecked(localData.getReminderStatus());

        if(!localData.getReminderStatus())
            ll_set_time.setAlpha(0.4f);

        reminderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                localData.setReminderStatus(isChecked);
                if(isChecked){
                    Log.d(TAG, "onCheckedChanged: true");
                    NotificationScheduler.setReminder(SettingsActivity.this,
                            AlarmReceiver.class, localData.get_hour(),
                            localData.get_min());
                    ll_set_time.setAlpha(1f);
                }
                else{
                    Log.d(TAG, "onCheckedChanged:false");
                    NotificationScheduler.cancelReminder(SettingsActivity.this, AlarmReceiver.class);
                    ll_set_time.setAlpha(0.4f);
                }
            }
        });

        ll_set_time.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(localData.getReminderStatus())
                    showTimePickerDialog(localData.get_hour(), localData.get_min());
            }
        });
    }
    public void update(View v){
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(),1,2);
    }
    private void showTimePickerDialog(int h,int m){

        LayoutInflater inflater=getLayoutInflater();
        View view= inflater.inflate(R.layout.timepicker_header, null);

        TimePickerDialog builder= new TimePickerDialog(this, R.style.DialogTheme,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int min) {
                        Log.d(TAG, "onTimeSet: hour" + hour);
                        Log.d(TAG, "onTimeSet: min" + min);
                        localData.set_hour(hour);
                        localData.set_min(min);
                        tvTime.setText(getFormatedTime(hour, min));
                        NotificationScheduler.setReminder(SettingsActivity.this, AlarmReceiver.class, localData.get_hour(), localData.get_min());
                    }
                }, h, m, false);
        builder.setCustomTitle(view);
        builder.show();

    }

    public String getFormatedTime(int h, int m){
        final String OLD_FORMAT= "HH:mm";
        final String NEW_FORMAT= "hh:mm a";

        String oldDateString= h + ":" + m;
        String newDateString="";

        try{
            SimpleDateFormat sdf= new SimpleDateFormat(OLD_FORMAT, getCurrentLocale());
            Date d= sdf.parse(oldDateString);
            sdf.applyPattern(NEW_FORMAT);
            newDateString= sdf.format(d);
        } catch (Exception e){
            e.printStackTrace();
        }
        return  newDateString;
    }
    @TargetApi(Build.VERSION_CODES.N)
    public Locale getCurrentLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0);
        } else {
            return getResources().getConfiguration().locale;
        }
    }



}
