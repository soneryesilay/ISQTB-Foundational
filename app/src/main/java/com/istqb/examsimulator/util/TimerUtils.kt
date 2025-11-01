package com.istqb.examsimulator.util

object TimerUtils {
    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    fun formatMinutes(minutes: Int): String {
        val mins = minutes / 60
        val secs = minutes % 60
        return String.format("%02d:%02d", mins, secs)
    }
}

