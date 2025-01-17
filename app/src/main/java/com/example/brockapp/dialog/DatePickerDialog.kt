package com.example.brockapp.dialog

import com.example.brockapp.interfaces.PeriodRangeImpl
import com.example.brockapp.viewModel.ActivitiesViewModel

import android.os.Bundle
import java.util.Calendar
import android.app.Dialog
import java.time.LocalDate
import android.widget.DatePicker
import android.app.DatePickerDialog
import androidx.fragment.app.DialogFragment

class DatePickerDialog(private val viewModel: ActivitiesViewModel): DialogFragment(), DatePickerDialog.OnDateSetListener {

    private val rangeUtil = PeriodRangeImpl()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireContext(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val date = LocalDate.of(year, month.plus(1), dayOfMonth)
        val range = rangeUtil.getDayRange(date)

        viewModel.getAllActivities(range.first, range.second)
    }
}