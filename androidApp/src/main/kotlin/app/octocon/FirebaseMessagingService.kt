package app.octocon

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import app.octocon.app.utils.ioDispatcher
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL

class OctoconFirebaseMessagingService : FirebaseMessagingService() {
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    // TODO(developer): Handle FCM messages here.
    Log.d(TAG, "From: ${remoteMessage.from}")

    // Check if message contains a notification payload.
    remoteMessage.notification?.let {
      Log.d(TAG, "Message Notification Body: ${it.body}")
      sendNotification(it.title ?: "Octocon", it.body.orEmpty(), it.imageUrl)
    }

    // Also if you intend on generating your own notifications as a result of a received FCM
    // message, here is where that should be initiated. See sendNotification method below.
  }
  // [END receive_message]

  // [START on_new_token]
  /**
   * Called if the FCM registration token is updated. This may occur if the security of
   * the previous token had been compromised. Note that this is called when the
   * FCM registration token is initially generated so this is where you would retrieve the token.
   */
  override fun onNewToken(token: String) {
    Log.d(TAG, "Refreshed token: $token")
  }

  private fun sendNotification(title: String, body: String, imageURL: Uri?) {
    /*val settings = getSettings(applicationContext)
    if (!settings.showPushNotifications) return*/

    val requestCode = 0
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    val pendingIntent = PendingIntent.getActivity(
      this,
      requestCode,
      intent,
      PendingIntent.FLAG_IMMUTABLE,
    )

    val channelId = "octocon-notification-channel"
    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val notificationBuilder = NotificationCompat.Builder(this, channelId)
      .setContentTitle(title)
      .setContentText(body)
      .setColor(0x4F89FD)
      .setAutoCancel(true)
      .setSound(defaultSoundUri)
      .setContentIntent(pendingIntent)
      .setSmallIcon(R.drawable.ic_stat_name)
      .apply {
        if (imageURL == null) {
          return@apply
        }

        runBlocking {
          withContext(ioDispatcher) {
            try {
              val url = URL(imageURL.toString())
              val input = url.openStream()
              BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
              null
            }
          }
        }?.let {
          setLargeIcon(it)
        }
      }

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Since android Oreo notification channel is needed.
    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
      channelId,
      "Push notifications",
      NotificationManager.IMPORTANCE_DEFAULT,
    )
    notificationManager.createNotificationChannel(channel)
    //}

    val notificationId = 0
    notificationManager.notify(notificationId, notificationBuilder.build())
  }

  companion object {
    private const val TAG = "OctoconFirebaseMsgService"
  }
}