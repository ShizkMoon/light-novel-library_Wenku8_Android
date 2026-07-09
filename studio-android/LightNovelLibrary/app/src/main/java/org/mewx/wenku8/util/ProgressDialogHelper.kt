package org.mewx.wenku8.util

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.util.Locale
import org.mewx.wenku8.R

/**
 * Helper to create Material 3 dialogs with an embedded progress indicator.
 */
class ProgressDialogHelper private constructor(
    private val dialog: AlertDialog,
    private val progressBar: LinearProgressIndicator,
    private val messageView: TextView,
    private val percentView: TextView,
    private val numberView: TextView
) {
    fun setProgress(progress: Int) {
        progressBar.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(progress, true)
            } else {
                progressBar.progress = progress
            }
            if (!progressBar.isIndeterminate) {
                updateProgressText(percentView, numberView, progress, progressBar.max)
            }
        }
    }

    fun setMaxProgress(max: Int) {
        progressBar.post {
            progressBar.isIndeterminate = false
            progressBar.max = max
            percentView.visibility = View.VISIBLE
            numberView.visibility = View.VISIBLE
            updateProgressText(percentView, numberView, progressBar.progress, max)
        }
    }

    fun dismiss() {
        progressBar.post {
            try {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            } catch (_: Exception) {
                // Ignore exceptions from dismissed dialogs or detached windows.
            }
        }
    }

    fun isShowing(): Boolean = dialog.isShowing

    fun setTitle(@StringRes titleId: Int) {
        progressBar.post { dialog.setTitle(titleId) }
    }

    fun setTitle(title: CharSequence) {
        progressBar.post { dialog.setTitle(title) }
    }

    fun setMessage(message: CharSequence) {
        progressBar.post { messageView.text = message }
    }

    companion object {
        /**
         * Create and show a progress dialog.
         *
         * @param context the context
         * @param message the message to display
         * @param indeterminate true for indeterminate, false for determinate
         * @param cancelable whether the dialog can be cancelled
         * @param cancelListener optional cancel listener
         * @return the ProgressDialogHelper instance
         */
        @JvmStatic
        fun show(
            context: Context,
            message: CharSequence,
            indeterminate: Boolean,
            cancelable: Boolean,
            cancelListener: DialogInterface.OnCancelListener?
        ): ProgressDialogHelper {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
            val messageView = view.findViewById<TextView>(R.id.progress_message)
            val progressBar = view.findViewById<LinearProgressIndicator>(R.id.progress_bar)
            val percentView = view.findViewById<TextView>(R.id.progress_percent)
            val numberView = view.findViewById<TextView>(R.id.progress_number)

            messageView.text = message

            if (indeterminate) {
                progressBar.isIndeterminate = true
                percentView.visibility = View.GONE
                numberView.visibility = View.GONE
            } else {
                progressBar.isIndeterminate = false
                progressBar.max = 1
                progressBar.progress = 0
                percentView.visibility = View.VISIBLE
                numberView.visibility = View.VISIBLE
                updateProgressText(percentView, numberView, 0, 1)
            }

            val builder = MaterialAlertDialogBuilder(context)
                .setView(view)
                .setCancelable(cancelable)

            if (cancelListener != null) {
                builder.setOnCancelListener(cancelListener)
            }

            val dialog = builder.create()
            dialog.show()

            return ProgressDialogHelper(dialog, progressBar, messageView, percentView, numberView)
        }

        /** Convenience overload that accepts a string resource for the message. */
        @JvmStatic
        fun show(
            context: Context,
            @StringRes messageResId: Int,
            indeterminate: Boolean,
            cancelable: Boolean,
            cancelListener: DialogInterface.OnCancelListener?
        ): ProgressDialogHelper = show(
            context,
            context.getString(messageResId),
            indeterminate,
            cancelable,
            cancelListener
        )

        private fun updateProgressText(
            percentView: TextView,
            numberView: TextView,
            progress: Int,
            max: Int
        ) {
            if (max <= 0) {
                percentView.text = "0%"
                numberView.text = String.format(Locale.getDefault(), "%d/%d", progress, max)
                return
            }

            val percent = (progress.toFloat() / max * 100).toInt()
            percentView.text = String.format(Locale.getDefault(), "%d%%", percent)
            numberView.text = String.format(Locale.getDefault(), "%d/%d", progress, max)
        }
    }
}
