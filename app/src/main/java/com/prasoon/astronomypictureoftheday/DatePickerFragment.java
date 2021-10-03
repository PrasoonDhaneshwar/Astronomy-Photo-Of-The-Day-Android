package com.prasoon.astronomypictureoftheday;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {
    Calendar c = Calendar.getInstance();
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH);
    int day = c.get(Calendar.DAY_OF_MONTH);
    // Account for Date Picker to show till today's date.
    private final long oneDayBefore = 1 * 24 * 60 * 60 * 1000L;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - oneDayBefore);
        return datePickerDialog;
    }
}
