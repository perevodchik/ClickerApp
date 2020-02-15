package com.perevodchik.clickerapp.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.perevodchik.clickerapp.R
import com.perevodchik.clickerapp.loge
import com.perevodchik.clickerapp.network.WebSocketClient0
import com.perevodchik.clickerapp.service.ClickerService
import kotlinx.android.synthetic.main.settings_activity.*
import java.lang.NumberFormatException

class SettingsActivity: AppCompatActivity() {
    private lateinit var shared: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor

    companion object {
        var mainAddress: String? = ""
        var userId: String? = ""
        var extraDelay: Long = 0L
    }

    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        shared = getSharedPreferences("clicker_app", Context.MODE_PRIVATE)

        mainAddress = shared.getString("main_address", "")
        userId = shared.getString("user_id", "")
        extraDelay = shared.getLong("extra_delay", 11111)


        main_server_param.setText(mainAddress)
        user_id_param.setText("$userId")
        extra_delay_param.setText("$extraDelay")

        apply_settings.setOnClickListener {
            edit = shared.edit()
            edit.putString("main_address", main_server_param.text.toString())
            edit.putString("user_id", user_id_param.text.toString())
            edit.putLong("extra_delay", java.lang.Long.parseLong(extra_delay_param.text.toString()))
            edit.commit()

            WebSocketClient0.url = "${main_server_param.text}"
            WebSocketClient0.id = "${user_id_param.text}"
            ClickerService.extraDelay = extraDelay
//            WebSocketClient0.reconnect()
//            WebSocketClient0.disconnect()
//            WebSocketClient0.connect(isLogin = true)
            finish()
        }

        close_settings.setOnClickListener {
            finish()
        }
    }

}