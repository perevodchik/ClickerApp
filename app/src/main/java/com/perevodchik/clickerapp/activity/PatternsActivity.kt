package com.perevodchik.clickerapp.activity

import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.perevodchik.clickerapp.DbHelper
import com.perevodchik.clickerapp.DeleteDialog
import com.perevodchik.clickerapp.R
import com.perevodchik.clickerapp.model.Pattern
import com.perevodchik.clickerapp.service.ClickerService
import kotlinx.android.synthetic.main.item_pattern.view.*
import kotlinx.android.synthetic.main.patterns_activity.*
import kotlinx.android.synthetic.main.pattern_settings.*

class PatternsActivity: AppCompatActivity() {
    private val list = mutableListOf<Pattern>().apply { addAll(DbHelper.getPatterns()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patterns_activity)
        patterns_recycler.layoutManager = LinearLayoutManager(this)
        patterns_recycler.adapter = Adapter(this, list)

        create_pattern.setOnClickListener {
            val name = "Pattern ${(0..99999).random()}"
            val id = DbHelper.createPattern(ContentValues().apply {
                put("name", name)
                put(DbHelper.columnPatternDuration, 1)
                }
            )
            list.add(0, Pattern(id = id, name = name))
            patterns_recycler.adapter?.notifyDataSetChanged()
        }
    }

    inner class Adapter(_activity: Activity, _list: MutableList<Pattern>): RecyclerView.Adapter<Holder>() {
        private val activity = _activity
        private val inflater = activity.layoutInflater
        private val list = _list

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(inflater.inflate(R.layout.item_pattern, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val entity = list[position]

            holder.deleteButton.setOnClickListener {
                DeleteDialog(
                    activity,
                    DialogInterface.OnClickListener { deleteDialog, _ ->
                        ClickerService.clickerService?.setPattern(null)
                        ClickerService.isRunning = false
                        DbHelper.deletePattern(entity)
                        list.remove(entity)
                        scenario_actions.adapter?.notifyDataSetChanged()
                        deleteDialog.cancel()
                    },
                    DialogInterface.OnClickListener { deleteDialog, _ ->
                        deleteDialog.cancel()
                    }
                ).show(supportFragmentManager, "delete_dialog")
            }

            holder.nameView.setText(list[position].name)
            holder.nameView.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val cv = ContentValues().apply {
                        put("name", s.toString())
                    }
                    DbHelper.updatePattern(list[position], cv)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            holder.timeView.setText("${list[position].executeTime}")
            holder.timeView.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val cv = ContentValues().apply {
                        put(DbHelper.columnPatternDuration, s.toString())
                    }
                    DbHelper.updatePattern(list[position], cv)
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            })

            holder.stepsCountView.text = activity.resources.getString(R.string.steps_count).plus(" ").plus(list[position].actions.size)
            holder.pane.setOnClickListener {
                Intent(this@PatternsActivity, ChangePatternActivity::class.java).apply {
                    putExtra("patternId", list[position].id)
                }.also { startActivity(it) }
            }
        }

    }

    inner class Holder(v: View): RecyclerView.ViewHolder(v) {
        val nameView: EditText = v.findViewById(R.id.input_pattern_name)
        val timeView: EditText = v.findViewById(R.id.input_pattern_time)
        val stepsCountView: TextView = v.findViewById(R.id.steps_count)
        val pane: LinearLayout = v.findViewById(R.id.pattern_item_pane)
        val deleteButton: ImageView = v.delete_pattern
    }

}