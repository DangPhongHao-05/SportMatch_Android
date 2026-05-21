package com.example.sportmatch.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 1. Hàm này tự chạy khi cài app lần đầu để lấy Mã định danh (Token) của máy
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Mã Token của máy này là: $token")
        // viết hàm gọi API gửi token này lên Server .NET để lưu vào Database
    }

    // 2. Hàm này hứng thông báo khi có tín hiệu gửi từ Server C# xuống
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Lấy tiêu đề và nội dung từ thông báo gửi từ C#
        val title = message.notification?.title ?: "Thông báo từ SPM App"
        val body = message.notification?.body ?: ""

        showNotification(title, body)
    }

    // Hàm hiển thị thông báo rớt từ thanh trạng thái xuống
    private fun showNotification(title: String, body: String) {
        val channelId = "sport_match_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 trở lên bắt buộc phải có Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Thông báo từ SPM App", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // có thể đổi thành icon riêng của app
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}