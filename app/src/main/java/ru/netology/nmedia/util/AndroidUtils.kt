package ru.netology.nmedia.util

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Context.shareText(title: String, text: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent =
        Intent.createChooser(intent, title)
    startActivity(shareIntent)
}

fun View.visibleOrGone(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}