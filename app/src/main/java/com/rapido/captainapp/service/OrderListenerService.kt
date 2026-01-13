package com.rapido.captainapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rapido.captainapp.MainActivity
import com.rapido.captainapp.R
import com.rapido.captainapp.data.local.SharedPrefsManager
import com.rapido.captainapp.domain.model.Order
import com.rapido.captainapp.domain.model.OrderStatus
import com.rapido.captainapp.domain.usecase.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class OrderListenerService() : Service() {
//    by inject() is a lazy delegate
//    Dependencies are injected when first accessed, not when service is created
//    Uses lazy property delegation under the hood
    private val orderRepository: OrderRepository by inject()
    private val sharedPrefsManager: SharedPrefsManager by inject()

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var listenerJob: Job? = null

    private var currentOrder: Order? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()

        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())

        serviceScope.launch {
            sharedPrefsManager.saveServiceRunning(true)
        }

        startListeningForOrders()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
//        If system kills service (low memory), restart it later
//        Restart with null intent
        return START_STICKY
    }

    private fun createNotificationChannels() {
//        Channels only exist in Android 8+, crashes on older versions
//        Build.VERSION_CODES.O:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel for foreground service notification
            val foregroundChannel = NotificationChannel(
                CHANNEL_ID_FOREGROUND,
                "Order Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when you are on duty"
            }
//description: Longer explanation (shown in settings)

            // Channel for new order notifications
            val orderChannel = NotificationChannel(
                CHANNEL_ID_ORDER,
                "New Orders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for new delivery orders"
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(foregroundChannel)
            notificationManager.createNotificationChannel(orderChannel)
        }
    }

    private fun startListeningForOrders() {

        listenerJob = serviceScope.launch {
            orderRepository.getPendingOrder().collectLatest { order ->
                Log.d(TAG, "Pending order: ${order?.id ?: "null"}")

                when {
                    order != null && currentOrder?.id != order.id && order.status == OrderStatus.ASSIGNED -> {
                        // New order arrived
                        currentOrder = order
                        showNewOrderNotification(order)
                        playNotificationSound()
                    }
                    order == null && currentOrder != null -> {
                        // Order cleared (accepted/rejected)
                        dismissNewOrderNotification()
                        currentOrder = null
                    }
                }
            }
        }
    }

    private fun showNewOrderNotification(order: Order) {
        Log.d(TAG, "Showing notification for order: ${order.id}")
//        - `FLAG_ACTIVITY_NEW_TASK` - Create new task if needed (required from non-Activity context)
//- `FLAG_ACTIVITY_CLEAR_TOP` - If MainActivity is already open, bring it to front (don't create duplicate)
        // Intent to open app
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            putExtra(EXTRA_ORDER_ID, order.id)
            putExtra(EXTRA_SHOW_ORDER_DIALOG, true)
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ORDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔔 New Order!")
            .setContentText("${order.id} • ₹${order.amount} • ${order.distance}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        """
                        Order: ${order.id}
                        Customer: ${order.customerName}
                        Pickup: ${order.pickupAddress}
                        Delivery: ${order.deliveryAddress}
                        Amount: ₹${order.amount} • ${order.distance}
                        """.trimIndent()
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .setFullScreenIntent(openAppPendingIntent, true)
            .build()

        notificationManager.notify(ORDER_NOTIFICATION_ID, notification)
    }

    private fun dismissNewOrderNotification() {
        notificationManager.cancel(ORDER_NOTIFICATION_ID)
    }

    private fun playNotificationSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone.play()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND)
            .setContentTitle("Captain CMP App - On Duty")
            .setContentText("Listening for new orders...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        dismissNewOrderNotification()
        listenerJob?.cancel()

        serviceScope.launch {
            sharedPrefsManager.saveServiceRunning(false)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "OrderListenerService"

        private const val CHANNEL_ID_FOREGROUND = "order_listener_channel"
        private const val CHANNEL_ID_ORDER = "new_order_channel"

        private const val FOREGROUND_NOTIFICATION_ID = 1001
        private const val ORDER_NOTIFICATION_ID = 2001

//        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_SHOW_ORDER_DIALOG = "show_order_dialog"

        fun start(context: Context) {
            val intent = Intent(context, OrderListenerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, OrderListenerService::class.java)
            context.stopService(intent)
        }
    }
}