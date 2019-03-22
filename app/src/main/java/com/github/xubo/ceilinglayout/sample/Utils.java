package com.github.xubo.ceilinglayout.sample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Author：xubo
 * Time：2019-03-20
 * Description：字符串工具类
 */

public class Utils {
    /**
     * dp转px
     * @param context
     * @param dpValue
     * @return
     */
    public static float dpTopx(Context context, float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 获取文本的宽度
     *
     * @return
     */
    public static int getTextWidth(String text, Paint paint) {
        int textWidth = 0;
        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);
            float[] widths = new float[1];
            String srt = String.valueOf(ch);
            paint.getTextWidths(srt, widths);
            textWidth += widths[0];
        }
        return textWidth;
    }

    /**
     * 获取文本的高度(int)
     *
     * @return
     */
    public static int getTextHeightForInt(Paint paint) {
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        return fontMetrics.bottom - fontMetrics.top;
    }

    /**
     * 获取屏幕宽
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 状态颜色且状态栏字符变深
     * @param activity
     * @param color
     */
    public static void setColor(Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(color);
        }
    }
}
