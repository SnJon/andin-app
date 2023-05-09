package ru.netology.nmedia.util

import android.app.AlertDialog
import android.content.Context
import ru.netology.nmedia.R

object AlarmDialog {
    fun showDialog(message: String, context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(context.getString(R.string.dialog_error_title))
        alertDialogBuilder.setMessage(message)

        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}