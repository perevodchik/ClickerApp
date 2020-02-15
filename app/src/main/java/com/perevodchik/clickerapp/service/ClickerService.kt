package com.perevodchik.clickerapp.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Notification
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Path
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.AbsoluteLayout
import android.widget.TextView
import android.widget.Toast
import com.perevodchik.clickerapp.*
import com.perevodchik.clickerapp.activity.MainActivity
import com.perevodchik.clickerapp.model.Action
import com.perevodchik.clickerapp.model.Pattern
import com.perevodchik.clickerapp.model.pattern.Base
import com.perevodchik.clickerapp.network.WebSocketClient0
import java.lang.Exception
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.HashMap


class ClickerService : AccessibilityService() {
    private lateinit var pane: AbsoluteLayout
    @SuppressLint("UseSparseArrays")
    private val map0 = HashMap<Action, Array<View?>>()
    private var pattern: Pattern? = null
    private var handler: Handler = Handler()
    private lateinit var shared: SharedPreferences

    companion object {
        private const val LIVE_ME_PACKAGE = "com.cmcm.live"
        var clickerService: ClickerService? = null
        var isRunning: Boolean = false
        var extraDelay: Long = 0L
        private var executeTime: Long = 0
        private var endTime: Long = 0
        var isTest: Boolean = false
    }

    private fun performBase(base: Base) {
        val pattern = Pattern(-1, "empty", base.data.clicker_info.actions as MutableList<Action>)
        val link = base.data.link
        executeTime = base.data.patternExecuteDuration

        val intent = Intent(Intent.ACTION_VIEW,  Uri.parse(link))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            intent.setPackage(LIVE_ME_PACKAGE)
            startActivity(intent)
        } catch (exxx: ActivityNotFoundException) {
            startActivity(intent)
        }

        handler.post{
            "start after extra delay".loge("extra delay")
            setPattern(pattern)
            start()
        }
    }

    fun addBase(base: Base) {
        performBase(base)
    }

    fun setPattern(_pattern: Pattern?, _isTest: Boolean = false) {
        isTest = _isTest
        if(isTest) {
            PreviewTapService.previewService?.toggleFlags(0)
        }
        pane.removeAllViews()
        map0.clear()
        pattern = _pattern
        update()
    }

    var statusBarHeight = {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        result
    }

    private fun update() {
        if (pattern == null) {
            pane.removeAllViews()
            map0.clear()
            return
        }

        for (item in pattern!!.actions) {
            item.loge()
            if (map0[item] == null) {
                val array = Array<View?>(2) { null; null }

                array[0] = createView(item)

                if (item.action == "swipe" && !isTest) {
                    array[1] = createView(item, true)
                    pane.addView(array[1])
                }

                if(!isTest) pane.addView(array[0])
                map0[item] = array
            } else {
                if(!isTest)
                    continue
                map0[item]?.get(0)?.x = fromPercentage0(item.x, MainActivity.width).toFloat()
                map0[item]?.get(0)?.y = fromPercentage0(item.y, MainActivity.height).toFloat()
                map0[item]?.get(1)?.x = fromPercentage0(item.x1, MainActivity.width).toFloat()
                map0[item]?.get(1)?.y = fromPercentage0(item.y1, MainActivity.height).toFloat()
            }
        }
    }

    /**
     * perform single click action
     * @param action data
     */
    private fun click(action: Action): GestureDescription.Builder {
        val x = fromPercentage0(action.x, MainActivity.width) - 25f
        val y = fromPercentage0(action.y, MainActivity.height) + statusBarHeight() + 0f
        "tap".loge("ACTION")
        return GestureDescription.Builder().apply {
            addStroke(
                GestureDescription.StrokeDescription(
                    Path().apply { moveTo(if(x < 1) 1f else x, if(y < 1) 1f else y) },
                    action.startDelay,
                    action.duration
                )
            )
        }
    }

    /**
     * perform double click action
     * @param action data
     */
    private fun doubleClick(action: Action): GestureDescription.Builder {
        val x = fromPercentage0(action.x, MainActivity.width) - 0f
        val y = fromPercentage0(action.y, MainActivity.height) + statusBarHeight() + 0f
        "double tap".loge("ACTION")
        return GestureDescription.Builder().apply {
            addStroke(
                GestureDescription.StrokeDescription(
                    Path().apply { moveTo(if(x < 1) 1f else x, if(y < 1) 1f else y) },
                    action.startDelay,
                    action.duration
                )
            )
            addStroke(
                GestureDescription.StrokeDescription(
                    Path().apply { moveTo(if(x < 1) 1f else x, if(y < 1) 1f else y) },
                    action.startDelay + action.duration + 150L,
                    action.duration
                )
            )
        }
    }

    /**
     * perform swipe action
     * @param action data
     */
    private fun swipe(action: Action): GestureDescription.Builder {
        val x = fromPercentage0(action.x, MainActivity.width) - 0f
        val x1 = fromPercentage0(action.x1, MainActivity.width) - 0f
        val y = fromPercentage0(action.y, MainActivity.height) + statusBarHeight() + 0f
        val y1 = fromPercentage0(action.y1, MainActivity.height) + statusBarHeight() + 0f
        "swipe".loge("ACTION")
        return GestureDescription.Builder().apply {
            addStroke(
                GestureDescription.StrokeDescription(
                    Path().apply {
                        moveTo(if(x < 1) 1f else x, if(y < 1) 1f else y)
                        lineTo(if(x1 < 1) 1f else x1, if(y1 < 1) 1f else y1)
                    },
                    action.startDelay,
                    action.duration
                )
            )
        }
    }

    private fun clear() {
        isTest = false
        PreviewTapService.previewService?.toggleColor(true)
        map0.clear()
        pattern = null
        try {
            pane.removeAllViews()
        } catch(err: Exception) {
            err.printStackTrace()
            "something wrong!:(".loge("STATE>EXCEPTION")
        }
        (getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager).killBackgroundProcesses(LIVE_ME_PACKAGE)
        "clear".loge("STATE>FUNCTION")
    }

    private fun checkTime(): Boolean {
        if(isTest) return true
        return if(Calendar.getInstance().timeInMillis < endTime) {
            "time true".loge("checkTime")
            true
        } else {
            "time false".loge("checkTime")
            clear()
            false
        }
    }

    fun stop() {
        isRunning = false
        executeTime = 0
        endTime = 0
        clear()
        WebSocketClient0.disconnect()
    }

    fun start() {
        executeTime.loge("executor time")
        "start".loge("start")
        if (map0.isNullOrEmpty())
            return
        handler = Handler()
        val iterator = map0.keys.sortedBy { it.position }.listIterator()
        handler.postDelayed({
            PreviewTapService.previewService?.toggleColor(false)
            isRunning = true
            endTime = Calendar.getInstance().timeInMillis + executeTime * 1000
            startZero(iterator, null)
        }, shared.getLong("extra_delay", 100L))
    }

    private fun startZero(i: ListIterator<Action>, _action: Action?, counter: Int = 0) {
        if (!isRunning) return
        if (_action != null && (counter > 1 || _action.loop)) {
            _action.loge("STATE>START_ZERO")

            if(!checkTime()) {
                return
            }

            val g: GestureDescription.Builder = when (_action.action) {
                "double tap" -> doubleClick(_action)
                "tap" -> click(_action)
                "swipe" -> swipe(_action)
                else -> click(_action)
            }

            clickerService?.dispatchGesture(g.build(), object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    if (counter > 0)
                        startZero(i, _action, if (_action.loop) counter else counter - 1)
                }
            }, null)

        } else if (i.hasNext()) {
            if(!checkTime()) {
                clear()
                return
            }
            val action = i.next()
            if (!action.enable) {
                startZero(i, null)
            } else {

                val g: GestureDescription.Builder = when (action.action) {
                    "double tap" -> doubleClick(action)
                    "click" -> click(action)
                    "swipe" -> swipe(action)
                    else -> click(action)
                }

                clickerService?.dispatchGesture(g.build(), object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        startZero(i, action, action.repeat)
                    }
                }, null)
            }
        } else {
            PreviewTapService.previewService?.toggleFlags(1)
            if(isTest) {
                PreviewTapService.previewService?.toggleColor(true)
                return
            }
            if (checkTime()) {
                "repeat pattern".logi("STATE")
                val iterator = map0.keys.sortedBy { it.position }.listIterator()
                startZero(iterator, null)
            } else {
                (getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager).killBackgroundProcesses(LIVE_ME_PACKAGE)
                Toast.makeText(clickerService!!, "Pattern end", Toast.LENGTH_SHORT)
                    .show()
                clickerService?.clear()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createView(data: Action, isSecondView: Boolean = false): View {
        val prefix = when {
            isSecondView -> "e"
            data.action == "swipe" -> "s"
            else -> ""
        }
        return TextView(this).apply {
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(resources.getColor(R.color.colorAccent, null))
            textSize = 18f
            width = 75
            height = 75
            alpha = 0.5f
            x = fromPercentage0(if (isSecondView) data.x1 else data.x, MainActivity.width) - 35f
            y = fromPercentage0(if (isSecondView) data.y1 else data.y, MainActivity.height) - 35f
            text = "${data.position}" + prefix
            background = resources.getDrawable(
                if (isSecondView) R.drawable.item_tap_second else R.drawable.item_tap_first,
                null
            )
            setOnClickListener {
                loge("click me! $text")
            }
        }
    }

    fun updateItem(data: Action) {
        var v: Array<View?>? = null

        for (e in map0.entries)
            if (e.key.id == data.id) {
                v = e.value
                break
            }
        if (v != null) {
            v[0]?.x = data.x
            v[0]?.y = data.y
            v[1]?.x = data.x1
            v[1]?.y = data.y1
        } else {
            v = Array(2) { createView(data); createView(data, true) }
            map0[data] = v
            pane.addView(v[0])
            if (data.action == "swipe")
                pane.addView(v[1])
        }
    }

    override fun onInterrupt() {}
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    @SuppressLint("ClickableViewAccessibility")
    override fun onServiceConnected() {
        super.onServiceConnected()
        "onServiceConnected".loge()
        clickerService = this
        shared = getSharedPreferences("clicker_app", Context.MODE_PRIVATE)
        val windowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        pane = AbsoluteLayout(this)

        pane.setOnTouchListener { _, event ->
                "action ${event.action}; X ${event.x}; Y ${event.y}; rawX ${event.rawX}; rawY ${event.rawY}".loge(
                    "touch"
                )
            true
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            ,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP
        windowManager.addView(pane, params)

        //performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        "AutoClickService onUnbind".loge()
        clickerService = null
        return super.onUnbind(intent)
    }


    override fun onDestroy() {
        "AutoClickService onDestroy".loge()
        clickerService = null
        super.onDestroy()
    }
}