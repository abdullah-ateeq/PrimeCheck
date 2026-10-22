package com.example.primecheck.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

object ShareHelper {

    fun copyToClipboard(context: Context, text: String, label: String = "PrimeCheck Result") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    fun shareText(context: Context, text: String, title: String = "Share PrimeCheck Result") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }
}
