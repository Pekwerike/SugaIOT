package com.example.sugaiot.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sugaiot.MainActivity
import com.example.sugaiot.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/*
NotificationUtils class is responsible for organizing core functions needed for SugaIOT
notification configurations
*/
@Singleton
class NotificationUtils @Inject constructor(
    private val notificationManager: NotificationManager,
    @ApplicationContext private val context: Context
) {

    fun createGlucoseSensorCommunicationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val glucoseSensorNotificationChannel = NotificationChannel(
                context.getString(R.string.communicating_with_glucose_sensor_notification_id),
                context.getString(R.string.communicating_with_glucose_sensor_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
                .apply {
                    description = "This notification channel is responsible for alerting you that ZipBolt is sharing files on the background"
                    setShowBadge(false)
                }
            notificationManager.createNotificationChannel(glucoseSensorNotificationChannel)
        }
    }


    fun configureGlucoseSensorCommunicationNotification(): Notification {
        val openMainActivityPendingIntent: PendingIntent =
            Intent(context, MainActivity::class.java).let {
                it.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                PendingIntent.getActivity(
                    context,
                    1010,
                    it,
                    0
                )
            }

        return NotificationCompat.Builder(context, context.getString(R.string.communicating_with_glucose_sensor_notification_id))
            .apply {
                setContentTitle(context.getString(R.string.communicating_with_glucose_sensor_notification_title))
                setContentText(context.getString(R.string.communicating_with_glucose_sensor_notification_text))
                setContentIntent(openMainActivityPendingIntent)
                setSmallIcon(R.drawable.ic_baseline_ac_unit_24)
                priority = NotificationCompat.PRIORITY_DEFAULT
            }.build()
    }
}