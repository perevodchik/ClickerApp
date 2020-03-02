package com.perevodchik.clickerapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*


class Logger {

    companion object {
        fun writeLog(ctx: Context, data: String) {
            try {
                val name = "clicker_log0"
                val fileOutputStream = ctx.openFileOutput(name, Context.MODE_PRIVATE)
                fileOutputStream.write(data.toByteArray())
                Toast.makeText(ctx, "save to -> ${ctx.filesDir}/name", Toast.LENGTH_LONG).show()

                "${ctx.filesDir} $name".loge("write to ->")

                readLog(ctx)
            } catch (e: FileNotFoundException){
                e.printStackTrace()
            }catch (e: NumberFormatException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        @SuppressLint("SetTextI18n")
        fun readLog(ctx: Context) {
            try {
                val fis = ctx.openFileInput("clicker_log0")
                val isr = InputStreamReader(fis)
                val br = BufferedReader(isr)
                val sb = StringBuilder()

                var text: String? = "error"

                while(text != null) {
                    text = br.readLine()
                    if (text == null) break
                    sb.append(text)
                }

                (ctx as Activity).log_field.setText(sb.toString())
                ctx.log_field.visibility = View.VISIBLE
            } catch(exx: Exception) {
                (ctx as Activity).log_field.setText("No such log file")
                exx.printStackTrace()
            }
        }
    }

}