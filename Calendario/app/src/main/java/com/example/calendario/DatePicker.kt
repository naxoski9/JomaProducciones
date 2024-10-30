package com.example.calendario

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

// Clase para el selector de fecha
class DatePicker(private val onDateSelectedListener: (day: Int, month: Int, year: Int) -> Unit)
    : DialogFragment(), DatePickerDialog.OnDateSetListener {

    // Método  cuando se selecciona una fecha
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        onDateSelectedListener(dayOfMonth, month, year)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireActivity(), this, year, month, day)

        // Establecer la fecha máxima como la fecha actual
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        return datePickerDialog
    }
}
