package com.perevodchik.clickerapp

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DeleteDialog(_context: Context, _onPositive: DialogInterface.OnClickListener, _onNegative: DialogInterface.OnClickListener): DialogFragment() {
    private val context0 = _context
    private val onPositive = _onPositive
    private val onNegative = _onNegative

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context0)
        builder.setMessage(R.string.delete_task)
            .setPositiveButton(R.string.delete, onPositive)
            .setNegativeButton(R.string.cancel, onNegative)
        return builder.create()
    }
}