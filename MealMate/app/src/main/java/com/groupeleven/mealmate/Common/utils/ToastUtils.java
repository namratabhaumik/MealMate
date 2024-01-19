package com.groupeleven.mealmate.Common.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Utility class for displaying Toast messages.
 */
public class ToastUtils {
    /**
     * Displays a long-duration toast message.
     *
     * @param context The context in which the toast should be displayed.
     * @param message The message to be displayed in the toast.
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}