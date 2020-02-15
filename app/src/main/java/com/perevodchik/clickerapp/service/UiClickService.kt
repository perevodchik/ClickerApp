package com.perevodchik.clickerapp.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.FrameLayout
import com.perevodchik.clickerapp.R
import com.perevodchik.clickerapp.loge
import com.perevodchik.clickerapp.network.WebSocketClient0
import kotlinx.android.synthetic.main.overlay_button.view.*
import kotlin.system.exitProcess


class UiClickService : Service() {
    private var mRootView: View? = null
    private val windowManager: WindowManager
        get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

    companion object {
        var uiService: UiClickService? = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createOverlay(root: View, clickListener: (View) -> Unit?) {
        val windowLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        windowLayoutParams.apply {
            gravity = Gravity.CENTER
            x = 0
            y = 0
        }

        val windowManager = windowManager
        windowManager.addView(root, windowLayoutParams)

        val container = root.findViewById<FrameLayout>(R.id.overlay_image_container)

        container.toggle_service.setOnClickListener {
            if (!WebSocketClient0.isRunning) {
                "run".loge("UIClickerService")
//                WebSocketClient0.connect(isLogin = true)
                WebSocketClient0.connect(isLogin = true)
                WebSocketClient0.isRunning = true
                container.toggle_service.text = resources.getString(R.string.stop)
            }
            else {
                "stop".loge("UIClickerService")
                WebSocketClient0.disconnect()
                ClickerService.clickerService?.stop()
                WebSocketClient0.isRunning = false
                container.toggle_service.text = resources.getString(R.string.start)
//                exitProcess(0)
            }
            WebSocketClient0.isRunning.loge("ws client running")
        }
        container.settings_btn.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("clicker://app.com")
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            ClickerService.clickerService?.stop()
        }

        container.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()
            private var isClick = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = windowLayoutParams.x
                        initialY = windowLayoutParams.y

                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                    }
                    MotionEvent.ACTION_UP -> {
                        val dX = (event.rawX - initialTouchX).toInt()
                        val dY = (event.rawY - initialTouchY).toInt()

                        isClick = dX < 10 && dY < 10
                    }
                    MotionEvent.ACTION_MOVE -> {
                        windowLayoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        windowLayoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(root, windowLayoutParams)
                    }
                    else -> {
                    }
                }

                if (isClick) {
                    clickListener(container)
                }
                return true
            }
        })
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mRootView = LayoutInflater.from(this).inflate(R.layout.overlay_button, null)
        mRootView?.let {
            createOverlay(it) {
            }
        }
        uiService = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mRootView != null) {
            windowManager.removeView(mRootView)
        }
        uiService = null
    }

}
