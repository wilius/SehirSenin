package org.alexiwilius.sehirsenin.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.alexiwilius.sehirsenin.R;

import java.util.Calendar;

/**
 * Created by AlexiWilius on 8.11.2015.
 */
public class DepartureDaySelect extends Dialog {
    public int day;

    public DepartureDaySelect(Context context, int day) {
        super(context);
        this.day = day;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_day);
        if (day == Calendar.SATURDAY)
            getRB(R.id.saturday).setChecked(true);
        else if (day == Calendar.SUNDAY)
            getRB(R.id.sunday).setChecked(true);
        else
            getRB(R.id.weekdays).setChecked(true);

        findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ((RadioGroup) findViewById(R.id.day_select_wrapper)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.saturday)
                    day = Calendar.SATURDAY;
                else if (checkedId == R.id.sunday)
                    day = Calendar.SUNDAY;
                else
                    day = Calendar.MONDAY;
            }
        });
    }

    public RadioButton getRB(int resId) {
        return (RadioButton) findViewById(resId);
    }
}
