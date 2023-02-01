package com.strizhonovapps.lexixapp.view.wordlist

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.EditText
import com.strizhonovapps.lexixapp.R

class ListDrawerDialogFactory(private val activity: Activity) {

    fun getEraseConfirmationDialog(onPositiveButtonFn: () -> Unit): AlertDialog =
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.dialog__content__clear_all))
            .setMessage(activity.getString(R.string.dialog__content__are_you_sure))
            .setPositiveButton(activity.getString(R.string.dialog__content__yes)) { _, _ -> onPositiveButtonFn() }
            .setNegativeButton(activity.getString(R.string.dialog__content__cancel), null)
            .create()

    fun getSeparatorDialog(
        listener: DialogInterface.OnClickListener,
        separatorEditText: EditText
    ): AlertDialog = AlertDialog.Builder(activity)
        .setTitle(activity.getString(R.string.dialog__content__insert_separator))
        .setMessage(activity.getString(R.string.dialog__content__separator_message))
        .setView(separatorEditText)
        .setPositiveButton(activity.getString(R.string.dialog__content__done), listener)
        .setNegativeButton(activity.getString(R.string.dialog__content__cancel), null)
        .create()


    fun getResetProgressDialog(onPositiveButtonFn: () -> Unit): AlertDialog =
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.dialog__content__reset_progress))
            .setMessage(activity.getString(R.string.dialog__content__are_you_sure))
            .setPositiveButton(activity.getString(R.string.dialog__content__yes)) { _, _ -> onPositiveButtonFn() }
            .setNegativeButton(activity.getString(R.string.dialog__content__cancel), null)
            .create()

}