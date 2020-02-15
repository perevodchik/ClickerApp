package com.perevodchik.clickerapp.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import com.google.gson.GsonBuilder
import com.perevodchik.clickerapp.R
import com.perevodchik.clickerapp.activity.MainActivity
import com.perevodchik.clickerapp.loge
import com.perevodchik.clickerapp.model.Action
import com.perevodchik.clickerapp.model.Pattern
import com.perevodchik.clickerapp.model.export.Data
import com.perevodchik.clickerapp.model.export.Export
import com.perevodchik.clickerapp.model.export.Templates
import com.perevodchik.clickerapp.network.WebSocketClient0
import com.perevodchik.clickerapp.toPercentage
import kotlinx.android.synthetic.main.overlay_edit_modal.view.*
import kotlinx.android.synthetic.main.overlay_export_modal.view.*
import kotlinx.android.synthetic.main.overlay_preview_control.view.*


class PreviewTapService : Service() {
    private var rootView: View? = null
    private var modalView: View? = null
    private var exportView: View? = null
    private val datas = mutableListOf<PreviewItemData>()
    private var wm: WindowManager? = null
    private var id = 1L
    companion object {
        var previewService: PreviewTapService? = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        id = 1L
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
        }

        rootView = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.overlay_preview_control,
            null,
            false
        )
        rootView?.apply {
            run_preview_view.setOnClickListener {
                val pat = Pattern(id = -1, name = "")
                for (item in datas)
                    pat.actions.add(item.action)
                ClickerService.clickerService?.setPattern(pat, true)
                ClickerService.clickerService?.start()
            }
            close_preview_service.setOnClickListener { stopSelf() }
            add_preview_view.setOnClickListener { addView() }
        }
        wm?.addView(rootView, params)
        rootView?.setOnTouchListener(createTouchListener(rootView!!, null))
        previewService = this
    }

    private fun createTouchListener(
        view: View,
        previewData: PreviewItemData?,
        isSecondView: Boolean = false
    ): View.OnTouchListener {
        return object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()
            private val lp = view.layoutParams as WindowManager.LayoutParams

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = lp.x
                        initialY = lp.y

                        initialTouchX = event.rawX
                        initialTouchY = event.rawY

                        lp.x.loge("lp x")
                        lp.y.loge("lp y")
                        event.rawX.loge("rawX0")
                        event.rawY.loge("rawY0")
                    }
                    MotionEvent.ACTION_UP -> {
                        val dX = (event.rawX - initialTouchX).toInt()
                        val dY = (event.rawY - initialTouchY).toInt()
                        event.rawX.loge("rawX1")
                        event.rawY.loge("rawY1")
                    }
                    MotionEvent.ACTION_MOVE -> {
                        lp.x = initialX + (event.rawX - initialTouchX).toInt()
                        lp.y = initialY + (event.rawY - initialTouchY).toInt()

                        wm?.updateViewLayout(view, lp)
                        event.rawX.loge("rawX")
                        event.rawY.loge("rawY")

                        if (!isSecondView) {
                            previewData?.action?.x =
                                toPercentage(event.rawX.toInt(), MainActivity.width)
                            previewData?.action?.y =
                                toPercentage(event.rawY.toInt(), MainActivity.height)
                        } else {
                            previewData?.action?.x1 =
                                toPercentage(event.rawX.toInt(), MainActivity.width)
                            previewData?.action?.y1 =
                                toPercentage(event.rawY.toInt(), MainActivity.height)
                        }
                    }
                }
                return true
            }
        }
    }

    fun toggleColor(isEnd: Boolean) {
        Toast.makeText(
            this,
            if (isEnd) resources.getString(R.string.test_pattern_end) else resources.getString(R.string.test_pattern_start),
            Toast.LENGTH_LONG
        ).show()
        isEnd.loge("isEnd")
        rootView?.run_preview_view?.setBackgroundColor(
            resources.getColor(
                if (isEnd) R.color.outerSpace else R.color.mintCream,
                null
            )
        )
    }

    private fun addView() {
        PreviewItemData(Action(id = id++, position = datas.size + 1, patternId = -1)).also {
            it.views[0] = createView(it)
            datas.add(it)
        }
    }

    fun toggleFlags(type: Int = 0) {
        for (data in datas) {
            if(data.views[0] != null) {
                (data.views[0]?.layoutParams as WindowManager.LayoutParams).flags =
                    if (type == 0)
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    else WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                wm?.updateViewLayout(data.views[0], data.views[0]?.layoutParams)
            }
            if (data.views[1] != null) {
                (data.views[1]?.layoutParams as WindowManager.LayoutParams).flags =
                    if (type == 0)
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    else WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                wm?.updateViewLayout(data.views[1], data.views[1]?.layoutParams)
            }
        }
    }

    private fun deleteView(previewData: PreviewItemData) {
        wm?.removeView(previewData.views[0])
        if (previewData.views[1] != null)
            wm?.removeView(previewData.views[1])
        datas.remove(previewData)
        updatePositions()
    }

    @SuppressLint("SetTextI18n")
    private fun updatePositions() {
        for (c in 0 until datas.size) {
            val tmp = datas[c]
            tmp.action.position = c + 1
            if (tmp.views[0] != null)
                ((tmp.views[0] as LinearLayout).getChildAt(0) as TextView).text = "${c + 1}"
            if (tmp.views[1] != null)
                ((tmp.views[1] as LinearLayout).getChildAt(0) as TextView).text = "${c + 1}"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createView(previewData: PreviewItemData, isSecondView: Boolean = false): View {
        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                75,
                75,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                75,
                75,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }
        params.apply {
            gravity = Gravity.TOP or Gravity.START
            x = MainActivity.width / 2 - 32
            y = MainActivity.height/ 2 - 32
        }
        return LinearLayout(this).apply {
            background = resources.getDrawable(
                if (isSecondView) R.drawable.item_tap_second else R.drawable.item_tap_first,
                null
            )
            alpha = 0.7f
            gravity = Gravity.CENTER
            addView(TextView(this@PreviewTapService).apply {
                id = R.id.text_view_id
                text = "${previewData.action.position}" + (if (isSecondView) "e" else "")
                setTextColor(resources.getColor(R.color.mintCream, null))
                textSize = 16f
                setOnClickListener { if (modalView == null) createModal(previewData, isSecondView) }
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }
            .also { wm?.addView(it, params) }
            .apply { setOnTouchListener(createTouchListener(this, previewData, isSecondView)) }

    }

    @SuppressLint("InflateParams")
    private fun createExportModal() {
        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                0,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                0,
                PixelFormat.TRANSLUCENT
            )
        }
        exportView = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.overlay_export_modal,
            null,
            false
        )
        exportView?.apply {
            alpha = 0.9f
        }
        exportView?.apply {
            close_export.setOnClickListener {
                wm?.removeView(exportView)
                exportView = null
            }
            apply_export.setOnClickListener {
                if (input_pattern_time.text.isEmpty() || input_pattern_name.text.isEmpty())
                    return@setOnClickListener

                val actionList = mutableListOf<Action>()

                for (item in datas)
                    actionList.add(item.action)

                val export = Export(
                    Data(
                        "clicker_template", listOf(
                            Templates(
                                id = -1,
                                name = input_pattern_name.text.toString(),
                                actions = actionList,
                                patternExecuteDuration = input_pattern_time.text.toString().toLong()
                            )
                        )
                    )
                )
                val gson = GsonBuilder().create()
                val exportString = gson.toJson(export)

                exportString.loge("STATE>EXPORT_STRING")

                WebSocketClient0.connect(exportString)
//                WebSocketClient0.channel?.writeAndFlush(TextWebSocketFrame(exportString))
                WebSocketClient0.disconnect()

                wm?.removeView(exportView)
                exportView = null
                stopSelf()
            }
        }
        wm?.addView(exportView, params)
    }

    @SuppressLint("InflateParams")
    private fun createModal(previewData: PreviewItemData, isSecondView: Boolean = false) {
        previewData.action.loge("STATE>ACTION")
        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                0,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                0,
                PixelFormat.TRANSLUCENT
            )
        }
        modalView = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.overlay_edit_modal,
            null,
            false
        )
        modalView?.apply {
            alpha = 0.9f
        }
        modalView?.apply {
            close_modal_btn.setOnClickListener {
                wm?.removeView(modalView)
                modalView = null
            }
            export_modal_btn.setOnClickListener {
                wm?.removeView(modalView)
                modalView = null
                createExportModal()
            }
            delete_modal_btn.setOnClickListener {
                deleteView(previewData)
                wm?.removeView(modalView)
                modalView = null
            }
            val a = "tap,double tap,swipe".split(",").toTypedArray()
            val adapter = ArrayAdapter<String>(context, R.layout.item_spinner_action, a)
            action_param.adapter = adapter
            action_param.setSelection((adapter).getPosition(previewData.action.action))

            action_param.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    itemPos: Int,
                    id: Long
                ) {
                    val item = action_param.adapter.getItem(itemPos) as String
                    previewData.action.action = item

                    if (item.equals("swipe", true) && previewData.views[1] == null) {
                        previewData.views[1] = createView(previewData, true)
                    } else if (!item.equals("swipe", true) && previewData.views[1] != null) {
                        if (isSecondView) {
                            wm?.removeView(modalView)
                            wm?.removeView(previewData.views[1])
                            previewData.views[1] = null
                        } else if (previewData.views[1] != null) {
                            wm?.removeView(previewData.views[1])
                            previewData.views[1] = null
                        }
                    }
                }
            }

            loop_param.isChecked = previewData.action.loop
            loop_param.setOnCheckedChangeListener { _, isChecked ->
                previewData.action.loop = isChecked
            }

            enable_param.isChecked = previewData.action.enable
            enable_param.setOnCheckedChangeListener { _, isChecked ->
                previewData.action.enable = isChecked
            }

            repeat_param.setText("${previewData.action.repeat}")
            repeat_param.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    try {
                        previewData.action.repeat = s.toString().toInt()
                    } catch (exx: NumberFormatException) {
                        previewData.action.repeat = 1
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            start_delay_param.setText("${previewData.action.startDelay}")
            start_delay_param.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    try {
                        previewData.action.startDelay = s.toString().toLong()
                    } catch (exx: NumberFormatException) {
                        previewData.action.startDelay = 500
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            duration_param.setText("${previewData.action.duration}")
            duration_param.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    try {
                        previewData.action.duration = s.toString().toLong()
                    } catch (exx: NumberFormatException) {
                        previewData.action.duration = 100
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
        wm?.addView(modalView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        previewService = null
        wm?.removeView(rootView)

        if (modalView != null)
            wm?.removeView(modalView)

        if (exportView != null)
            wm?.removeView(exportView)

        for (prev in datas) {
            wm?.removeView(prev.views[0])
            if (prev.views[1] != null)
                wm?.removeView(prev.views[1])
        }
    }

    data class PreviewItemData(val action: Action, val views: Array<View?> = Array(2) { null; null }) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PreviewItemData

            if (action != other.action) return false
            if (!views.contentEquals(other.views)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = action.hashCode()
            result = 31 * result + views.contentHashCode()
            return result
        }
    }
}