package com.perevodchik.clickerapp.activity

import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.perevodchik.clickerapp.*
import com.perevodchik.clickerapp.model.Action
import com.perevodchik.clickerapp.model.Pattern
import com.perevodchik.clickerapp.model.export.Data
import com.perevodchik.clickerapp.model.export.Export
import com.perevodchik.clickerapp.model.export.Templates
import com.perevodchik.clickerapp.network.WebSocketClient0
import com.perevodchik.clickerapp.service.ClickerService
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import kotlinx.android.synthetic.main.item_action.view.*
import kotlinx.android.synthetic.main.pattern_settings.*


class ChangePatternActivity : AppCompatActivity() {
    private var position = 0
    private lateinit var pattern: Pattern

    companion object {
        const val ACTION_TYPES = "tap,double tap,swipe"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pattern_settings)

        val patternId = intent.getLongExtra("patternId", -1L)
        pattern = DbHelper.getPattern(patternId)

        for (i in pattern.actions) {
            if (i.position > position) position = i.position + 1
        }

        scenario_actions.adapter = Adapter(this, pattern.actions)
        scenario_actions.layoutManager = LinearLayoutManager(this)
        add_action.setOnClickListener {
            val id = DbHelper.createAction(ContentValues().apply {
                put(DbHelper.columnPatternId, pattern.id)
                put(DbHelper.columnPosition, pattern.actions.size)
                put(DbHelper.columnX, 0f)
                put(DbHelper.columnY, 0f)
                put(DbHelper.columnX1, 0f)
                put(DbHelper.columnY1, 0f)
                put(DbHelper.columnAction, "tap")
                put(DbHelper.columnRepeat, 1)
                put(DbHelper.columnStartDelay, 500)
                put(DbHelper.columnDuration, 100)
                put(DbHelper.columnLoop, 0)
                put(DbHelper.columnEnable, 1)
            })
            val action = Action(
                id = id,
                position = position++,
                patternId = patternId
            )
            pattern.actions.add(action)
            scenario_actions.adapter?.notifyDataSetChanged()
        }
        apply_action.setOnClickListener {
            ClickerService.clickerService?.setPattern(DbHelper.getPattern(pattern.name))
        }
        export.setOnClickListener {
            val pattern = DbHelper.getPattern(patternId)
            val export = Export(Data("clicker_template", listOf(Templates(id = pattern.id.toInt(), name = pattern.name, actions = pattern.actions as List<Action>, patternExecuteDuration = pattern.executeTime))))
            val gson = GsonBuilder().create()
            val exportString = gson.toJson(export)

            if(WebSocketClient0.isAuthorized) {
                WebSocketClient0.channel?.writeAndFlush(TextWebSocketFrame(exportString))
            } else Toast.makeText(this, "Please connect to server!", Toast.LENGTH_LONG).show()
        }
    }

    inner class Adapter(_activity: Activity, _list: MutableList<Action>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val list = _list
        private val activity = _activity
        private val inflater = activity.layoutInflater


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return Holder(inflater.inflate(R.layout.item_action, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = holder as Holder
            val entity: Action = list[position]

            // модалка для удаления екшна
            item.deleteButton.setOnClickListener {
                DeleteDialog(
                    activity,
                    DialogInterface.OnClickListener { deleteDialog, _ ->
                        DbHelper.deleteAction(entity)
                        pattern.actions.remove(entity)
                        scenario_actions.adapter?.notifyDataSetChanged()
                        deleteDialog.cancel()
                    },
                    DialogInterface.OnClickListener { deleteDialog, _ ->
                        deleteDialog.cancel()
                    }
                ).show(supportFragmentManager, "delete_dialog")
            }

            // позиция екшна в спике
            item.positionView.text = "${entity.position}"

            // количество повторов
            item.inputRepeat.setText("${entity.repeat}")
            item.inputRepeat.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    try {
                        entity.repeat = s.toString().toInt()
                        DbHelper.updateAction(
                            entity,
                            ContentValues().apply { put(DbHelper.columnRepeat, entity.repeat) })
                    } catch (e: Exception) {
                        entity.repeat = 0
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

            // ввод х
            item.inputX.setText(
                "${fromPercentage0(
                    entity.x,
                    MainActivity.width
                )}"
            )
            item.inputX.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    try {
                        val x = s.toString().toFloat()
                        entity.x = toPercentage0(x.toInt(), MainActivity.width)
                        DbHelper.updateAction(entity, ContentValues().apply {
                            put(
                                DbHelper.columnX,
                                entity.x
                            )
                        })
                    } catch (e: Exception) {
                        entity.x = 0f
                    }
                    ClickerService.clickerService?.updateItem(entity)
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

            // ввод у
            item.inputY.setText(
                "${fromPercentage0(
                    entity.y,
                    MainActivity.height
                )}"
            )
            item.inputY.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    try {
                        val y = s.toString().toFloat()
                        entity.y = toPercentage0(y.toInt(), MainActivity.height)
                        DbHelper.updateAction(entity, ContentValues().apply {
                            put(
                                DbHelper.columnY,
                                entity.y
                            )
                        })
                    } catch (e: Exception) {
                        entity.y = 0f
                    }
                    ClickerService.clickerService?.updateItem(entity)
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


            item.swipeView.visibility = if (entity.action == "swipe") View.VISIBLE else View.GONE
            // ввод х1 если выбран свайп
            item.inputX1.setText(
                "${fromPercentage0(
                    entity.x1,
                    MainActivity.width
                )}"
            )
            item.inputX1.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    try {
                        val x1 = s.toString().toFloat()
                        entity.x1 = toPercentage0(x1.toInt(), MainActivity.width)
                        DbHelper.updateAction(entity, ContentValues().apply {
                            put(
                                DbHelper.columnX1,
                                entity.x1
                            )
                        })
                    } catch (e: Exception) {
                        entity.x1 = 0f
                    }
                    ClickerService.clickerService?.updateItem(entity)
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

            // ввод у1 если выбран свайп
            item.inputY1.setText(
                "${fromPercentage0(
                    entity.y1,
                    MainActivity.height
                )}"
            )
            item.inputY1.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    try {
                        val y1 = s.toString().toFloat()
                        entity.y1 = toPercentage0(y1.toInt(), MainActivity.height)
                        DbHelper.updateAction(entity, ContentValues().apply {
                            put(
                                DbHelper.columnY1,
                                entity.y1
                            )
                        })
                    } catch (e: Exception) {
                        entity.y1 = 0f
                    }
                    ClickerService.clickerService?.updateItem(entity)
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

            // ввод дилей перед стартом выполнения
            item.inputDelay.setText("${entity.startDelay}")
            item.inputDelay.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    try {
                        entity.startDelay = s.toString().toLong()
                        DbHelper.updateAction(
                            entity,
                            ContentValues().apply {
                                put(
                                    DbHelper.columnStartDelay,
                                    entity.startDelay
                                )
                            })
                    } catch (e: Exception) {
                        entity.startDelay = 0
                    }
                    ClickerService.clickerService?.updateItem(entity)
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

            // ввод длительности тапа
            item.inputDuration.setText("${entity.duration}")
            item.inputDuration.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    try {
                        entity.duration = s.toString().toLong()
                        DbHelper.updateAction(
                            entity,
                            ContentValues().apply { put(DbHelper.columnDuration, entity.duration) })
                    } catch (e: Exception) {
                        entity.duration = 0
                    }
                    ClickerService.clickerService?.updateItem(entity)
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

            // список в типами тапа
            val a = ACTION_TYPES.split(",").toTypedArray()
            val adapter = ArrayAdapter<String>(
                this.activity,
                R.layout.item_spinner_action, a
            )
            item.inputAction.adapter = adapter
            item.inputAction.setSelection(
                (adapter).getPosition(
                    entity.action
                )
            )

            item.inputAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    itemPos: Int,
                    id: Long
                ) {
                    entity.action = item.inputAction.adapter.getItem(itemPos) as String
                    DbHelper.updateAction(
                        entity,
                        ContentValues().apply { put(DbHelper.columnAction, entity.action) })
                    item.swipeView.visibility =
                        if (entity.action == "swipe") View.VISIBLE else View.GONE
                }
            }

            // boolean если зацикленное действие
            item.inputLoop.isChecked = entity.loop
            item.inputLoop.setTextColor(
                activity.resources.getColor(
                    if (entity.loop) R.color.colorAccent else R.color.colorGunmetal,
                    null
                )
            )
            item.inputLoop.setOnCheckedChangeListener { _, isChecked ->
                entity.loop = isChecked
                DbHelper.updateAction(
                    entity,
                    ContentValues().apply { put(DbHelper.columnLoop, if (entity.loop) 1 else 0) })
                item.inputLoop.setTextColor(
                    activity.resources.getColor(
                        if (isChecked) R.color.colorAccent else R.color.colorGunmetal,
                        null
                    )
                )
                item.inputRepeat.setTextColor(
                    activity.resources.getColor(
                        if (isChecked) R.color.colorAccent else R.color.colorGunmetal,
                        null
                    )
                )
                item.inputRepeat.isEnabled = !isChecked
            }

            // boolean если действие активно (надо выполнять)
            item.inputEnable.isChecked = entity.enable
            item.inputEnable.setTextColor(
                activity.resources.getColor(
                    if (entity.enable) R.color.colorAccent else R.color.colorGunmetal,
                    null
                )
            )
            item.inputEnable.setOnCheckedChangeListener { _, isChecked ->
                entity.enable = isChecked
                DbHelper.updateAction(
                    entity,
                    ContentValues().apply {
                        put(
                            DbHelper.columnEnable,
                            if (entity.enable) 1 else 0
                        )
                    })
                item.inputEnable.setTextColor(
                    activity.resources.getColor(
                        if (isChecked) R.color.colorAccent else R.color.colorGunmetal,
                        null
                    )
                )
            }
        }
    }

    inner class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val positionView: TextView = v.position_param
        val inputAction: Spinner = v.action_param
        val inputX: EditText = v.x_param
        val inputY: EditText = v.y_param
        val inputX1: EditText = v.x1_param
        val inputY1: EditText = v.y1_param
        val inputRepeat: EditText = v.repeat_param
        val inputDelay: EditText = v.start_delay_param
        val inputDuration: EditText = v.duration_param
        val inputLoop: CheckBox = v.loop_param
        val inputEnable: CheckBox = v.enable_param
        val deleteButton: ImageView = v.delete_action
        val swipeView: LinearLayout = v.swipe_params
    }

}