package com.seagate.alto.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.seagate.alto.utils.ColorUtils;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class InfoPanelView extends ImageView {

    private final TextPaint mDayPaint = new TextPaint();
    private final TextPaint mMonthPaint = new TextPaint();
    private final Paint mColorPaint = new Paint();

    float mDayTextSize;
    float mMonthTextSize;
    String mDay;
    String mMonth;

    public InfoPanelView(Context context) {
        super(context);
        init();
    }

    public InfoPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InfoPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Random random = new Random();

        mDayTextSize = 120;
        mMonthTextSize = 100;

        mDayPaint.setColor(Color.WHITE);
        mDayPaint.setAntiAlias(true);

        mMonthPaint.setColor(Color.WHITE);
        mMonthPaint.setAntiAlias(true);

        mColorPaint.setStyle(Paint.Style.FILL);
        mColorPaint.setColor(ColorUtils.getCompanyColor(random.nextInt(100)));

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        draw(canvas, 128);
    }


    public void setBounds(Rect bounds) {
        this.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    public void setTimestamp(long timestamp) {
        mDay = getDayString(timestamp);
        mMonth = getMonthString(timestamp);
    }

    private void draw(Canvas canvas, int alphaBackground) {
        if (hasInfo()) {
            // draw background color
            // TODO: 1/29/16 set appropriate color here
            mColorPaint.setAlpha(alphaBackground);
            canvas.drawPaint(mColorPaint);

            // draw date
            int xDay = getWidth() / 2;
            int yDay = (int) (getHeight() / 2 - (mDayPaint.descent() + mDayPaint.ascent() / 2));
            int xMonth = getWidth() / 2;
            int yMonth = (int) (getHeight() / 2 - (mMonthPaint.descent() + mMonthPaint.ascent() / 2) + mMonthTextSize);

            if (mDay != null && mMonth != null) {
                mDayPaint.setTextAlign(Paint.Align.CENTER);
                mDayPaint.setTextSize(mDayTextSize);
                canvas.drawText(mDay, xDay, yDay, mDayPaint);

                mMonthPaint.setTextAlign(Paint.Align.CENTER);
                mMonthPaint.setTextSize(mMonthTextSize);
                canvas.drawText(mMonth, xMonth, yMonth, mMonthPaint);
            }
        }
    }

    public boolean hasInfo() {
        return true;
    }

    String getDayString(long timestamp) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(timestamp);
        return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    String getMonthString(long timestamp) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(timestamp);
        return calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
    }
}
