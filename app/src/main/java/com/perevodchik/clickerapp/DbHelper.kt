package com.perevodchik.clickerapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.perevodchik.clickerapp.model.Action
import com.perevodchik.clickerapp.model.Pattern


class DbHelper(context: Context?) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    companion object {
        const val DATABASE_NAME = "click.db"
        const val PATTERN_TABLE = "patterns"
        const val ACTION_TABLE = "actions"
        // !45
        const val DATABASE_VERSION = 5

        /** database column names START **/
        const val columnId = "id"
        const val columnName = "name"
        const val columnPatternId = "patternId"
        const val columnX = "x"
        const val columnY = "y"
        const val columnX1 = "x1"
        const val columnY1 = "y1"
        const val columnPosition = "position"
        const val columnAction = "action"
        const val columnDuration = "duration"
        const val columnPatternDuration = "patternDuration"
        const val columnStartDelay = "startDelay"
        const val columnRepeat = "repeat"
        const val columnLoop = "loop"
        const val columnEnable = "enable1"
        const val createTable = "CREATE TABLE IF NOT EXISTS"
        const val IPKA = "INTEGER PRIMARY KEY AUTOINCREMENT"
        const val INN = "INTEGER NOT NULL"
        const val RNN = "REALNOT NULL"
        const val TNN = "TEXT NOT NULL"

        /** database column names END **/

        var liteDatabase: SQLiteDatabase? = null

        fun getPattern(patternId: Long): Pattern {
            if (patternId < 0)
                throw Exception("Pattern ID must be positive")

            val c =
                liteDatabase!!.query(
                    PATTERN_TABLE,
                    null,
                    "id = ?",
                    arrayOf("$patternId"),
                    null,
                    null,
                    null
                )
            var pattern: Pattern? = null

            if (c.moveToFirst()) {
                val id = c.getColumnIndex(columnId)
                val name = c.getColumnIndex(columnName)
                val executeTime = c.getColumnIndex(columnPatternDuration)
                pattern = Pattern(id = c.getLong(id), name = c.getString(name), executeTime = c.getLong(executeTime))
                pattern.actions.addAll(getActions(pattern.id))
            }

            c.close()
            return pattern!!
        }

        fun getPattern(patternName: String): Pattern? {
            if (patternName.isEmpty())
                throw Exception("Pattern name must not be empty")

            val c =
                liteDatabase!!.query(
                    PATTERN_TABLE,
                    null,
                    "name = ?",
                    arrayOf(patternName),
                    null,
                    null,
                    null
                )
            var pattern: Pattern? = null

            if (c.moveToFirst()) {
                val id = c.getColumnIndex(columnId)
                val name = c.getColumnIndex(columnName)
                val executeTime = c.getColumnIndex(columnPatternDuration)
                pattern = Pattern(id = c.getLong(id), name = c.getString(name), executeTime = c.getLong(executeTime))
                pattern.actions.addAll(getActions(pattern.id))
            }

            c.close()
            return pattern
        }

        fun getPatterns(): MutableList<Pattern> {
            val list = mutableListOf<Pattern>()
            val c =
                liteDatabase!!.query(PATTERN_TABLE, null, null, null, null, null, "$columnId DESC")
            if (c.moveToFirst()) {
                do {
                    val id = c.getColumnIndex(columnId)
                    val name = c.getColumnIndex(columnName)
                    val executeTime = c.getColumnIndex(columnPatternDuration)
                    val entity = Pattern(
                        id = c.getLong(id),
                        name = c.getString(name),
                        executeTime = c.getLong(executeTime)
                    )
                    entity.actions.addAll(getActions(entity.id))
                    list.add(entity)
                } while (c.moveToNext())
            }
            c.close()
            return list
        }

        fun createPattern(values: ContentValues): Long {
            return liteDatabase!!.insert(PATTERN_TABLE, null, values)
        }

        fun deletePattern(p: Pattern) {
            liteDatabase!!.delete(PATTERN_TABLE, "$columnId = ?", arrayOf("${p.id}"))
            liteDatabase!!.delete(ACTION_TABLE, "$columnPatternId = ?", arrayOf("${p.id}"))
        }

        fun updatePattern(p: Pattern, values: ContentValues): Long {
            return liteDatabase!!.update(PATTERN_TABLE, values, "$columnId = ?", arrayOf("${p.id}"))
                .toLong()
        }

        private fun getActions(_patternId: Long): MutableList<Action> {
            val list = mutableListOf<Action>()
            val c =
                liteDatabase!!.query(
                    ACTION_TABLE,
                    null,
                    "$columnPatternId= ?",
                    arrayOf("$_patternId"),
                    null,
                    null,
                    "$columnPosition ASC"
                )
            if (c.moveToFirst()) {
                do {
                    val id = c.getColumnIndex(columnId)
                    val patternId = c.getColumnIndex(columnPatternId)
                    val position = c.getColumnIndex(columnPosition)
                    val action = c.getColumnIndex(columnAction)
                    val x = c.getColumnIndex(columnX)
                    val y = c.getColumnIndex(columnY)
                    val x1 = c.getColumnIndex(columnX1)
                    val y1 = c.getColumnIndex(columnY1)
                    val duration = c.getColumnIndex(columnDuration)
                    val startDelay = c.getColumnIndex(columnStartDelay)
                    val repeat = c.getColumnIndex(columnRepeat)
                    val loop = c.getColumnIndex(columnLoop)
                    val enable = c.getColumnIndex(columnEnable)

                    val entity = Action(
                        id = c.getLong(id),
                        position = c.getInt(position),
                        patternId = c.getLong(patternId),
                        action = c.getString(action),
                        x = c.getFloat(x),
                        y = c.getFloat(y),
                        x1 = c.getFloat(x1),
                        y1 = c.getFloat(y1),
                        duration = c.getLong(duration),
                        startDelay = c.getLong(startDelay),
                        repeat = c.getInt(repeat),
                        loop = c.getInt(loop) == 1,
                        enable = c.getInt(enable) == 1
                    )
                    list.add(entity)
                } while (c.moveToNext())
            }
            c.close()
            return list
        }

        fun createAction(values: ContentValues): Long {
            return liteDatabase!!.insert(ACTION_TABLE, null, values)
        }

        fun deleteAction(action: Action) {
            liteDatabase!!.delete(ACTION_TABLE, "$columnId = ?", arrayOf("${action.id}"))
        }

        fun updateAction(action: Action, values: ContentValues): Long {
            return liteDatabase!!.update(
                ACTION_TABLE,
                values,
                "$columnId = ?",
                arrayOf("${action.id}")
            ).toLong()
        }
    }

    init {
        liteDatabase = this.readableDatabase
    }

    override fun onCreate(db: SQLiteDatabase) {
        "pre".loge("onCreate")
        val sqlCreateTablePatterns =
            ("$createTable $PATTERN_TABLE ("
                    + "$columnId $IPKA, "
                    + "$columnName $TNN,"
                    + "$columnPatternDuration $INN);")


        val sqlCreateTableActions =
            ("$createTable $ACTION_TABLE (" + "$columnId $IPKA, " +
                    "$columnPatternId $INN, $columnPosition $INN, $columnAction $TNN, " +
                    "$columnX $RNN, $columnY $RNN, $columnX1 $RNN, $columnY1 $RNN, " +
                    "$columnDuration $INN, $columnStartDelay $INN, $columnRepeat $INN, $columnLoop $INN, $columnEnable $INN);")

        sqlCreateTablePatterns.loge("onCreate")
        sqlCreateTableActions.loge("onCreate")

        db.execSQL(sqlCreateTablePatterns)
        db.execSQL(sqlCreateTableActions)
        "post".loge("onCreate")
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        "pre".loge("onUpgrade")
        db.execSQL("DROP TABLE IF EXISTS $PATTERN_TABLE")
        db.execSQL("DROP TABLE IF EXISTS $ACTION_TABLE")
        onCreate(db)
        "post".loge("onUpgrade")
    }
}