package com.example.seizenplayer.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.seizenplayer.R

class ReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context!!, "notificationChannel")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("SeizenPlayer")
            .setContentText("IT'S TIME TO STOP.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        id++
        manager.notify(id, builder.build())
    }
    companion object {
        var id = 0
    }
}