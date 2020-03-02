package com.perevodchik.clickerapp.activity

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.perevodchik.clickerapp.*
import com.perevodchik.clickerapp.network.WebSocketClient0
import com.perevodchik.clickerapp.service.PreviewTapService
import com.perevodchik.clickerapp.service.UiClickService
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.JdkLoggerFactory
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var overlayControlService: Intent

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 42
        private const val ALERT_PERMISSION_REQUEST_CODE = 202
        private const val USAGE_STATS_PERMISSION_REQUEST_CODE = 228

        private var haveOverlayPermission = true
        private var haveAlertPermission = true
        private var haveStatsPermission = true

        var width: Int = 0
        var height: Int = 0
    }

    private fun someTest() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        width = displayMetrics.widthPixels
        height = displayMetrics.heightPixels

        width.loge("width")

        (width * 0.2).loge("from 20")
        (width * 0.5).loge("from 50")

        (((width * 0.2) / width) * 100).loge("from 20")
        (((width * 0.5) / width) * 100).loge("from 50")
    }

    private fun stopMyService(): Boolean = stopService(overlayControlService)

    private fun startMyService() {
        startService(overlayControlService)
    }

    private fun checkAccess(): Boolean {
        val string = getString(R.string.accessibility_service_id)
        val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val list = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        "show services".logd("start")
        for (id in list) {
            id.id.logd("service")
            if (string == id.id) {
                return true
            }
        }
        "show services".logd("end")
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_toggle_service.setOnClickListener {
            if(!stopMyService())
                startMyService()
        }

        write_log.setOnClickListener {
//            Logger.readLog(this)
        }

        // инициализация бд
        DbHelper(this)
        // интент с соверлей сервисом
        overlayControlService = Intent(this, UiClickService::class.java)

        // запрос разрешений
        requestPermissions()

        // проверка запущен ли AccessibilityService для кликов
        (if(checkAccess()) "accept" else "denied").logd("/.ClickerService")
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)

        add_scenario.setOnClickListener {
            val overlayIntent = Intent(this, PreviewTapService::class.java)
            startService(overlayIntent)
        }

        // активити с настройками
        start_settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val shared: SharedPreferences = getSharedPreferences("clicker_app", Context.MODE_PRIVATE)
        SettingsActivity.mainAddress = shared.getString("main_address", "ws://195.201.109.34:1488/ws")
        SettingsActivity.userId = shared.getString("user_id", "")
        SettingsActivity.extraDelay = shared.getLong("extra_delay", 5000)
        WebSocketClient0.url = SettingsActivity.mainAddress ?: ""
        WebSocketClient0.id = SettingsActivity.userId?: ""

        someTest()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(overlayControlService)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST_CODE -> {
                haveOverlayPermission = Settings.canDrawOverlays(this)
                showToast(if (haveOverlayPermission) "Yay overlay!" else "No overlay permission :(")
            }
            ALERT_PERMISSION_REQUEST_CODE -> {
                haveAlertPermission = Settings.canDrawOverlays(this)
                showToast(if (haveOverlayPermission) "Yay overlay!" else "No alert permission :(")
            }
        }
    }

    private fun requestPermissions() {
        requestOverlayPermission()
//        requestUsageStatsPermission()
    }

    /**
     * Overlay request
     */
    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.apply {
                setMessage(R.string.overlay_request)
                setCancelable(false)
                setNegativeButton(R.string.action_refuse) { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                setPositiveButton(R.string.action_proceed) { dialog, _ ->
                    dialog.dismiss()
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent,
                        OVERLAY_PERMISSION_REQUEST_CODE
                    )
                }
            }
            dialogBuilder.create().show()
        } else {
                haveOverlayPermission = true
            showToast("Yay overlay!")
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        val mgr = applicationContext
            .getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return mgr.checkOpNoThrow(
            "android:get_usage_stats",
            android.os.Process.myUid(), packageName
        ) == AppOpsManager.MODE_ALLOWED
    }

    private fun showToast(value: String) {
        Toast.makeText(this, value, Toast.LENGTH_LONG).show()
    }

}
