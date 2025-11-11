package com.example.ruralize.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MiniLineChartView extends View {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Float> values = new ArrayList<>();

    public MiniLineChartView(Context context) {
        super(context);
        init();
    }

    public MiniLineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiniLineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setColor(0xFF6B8E62);
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        axisPaint.setColor(0x336B8E62);
        axisPaint.setStrokeWidth(2f);
    }

    public void setData(List<Float> data) {
        values.clear();
        if (data != null) {
            values.addAll(data);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        canvas.drawLine(0, height - 6, width, height - 6, axisPaint);

        if (values.isEmpty()) {
            return;
        }

        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        for (Float value : values) {
            if (value == null) continue;
            if (value > max) max = value;
            if (value < min) min = value;
        }

        if (max == min) {
            max += 1f;
            min -= 1f;
        }

        float range = max - min;
        float stepX = width / Math.max(values.size() - 1, 1);

        Float previousX = null;
        Float previousY = null;
        for (int i = 0; i < values.size(); i++) {
            Float value = values.get(i);
            if (value == null) continue;
            float x = i * stepX;
            float normalized = (value - min) / range;
            float y = height - (normalized * (height - 12)) - 6;

            if (previousX != null && previousY != null) {
                canvas.drawLine(previousX, previousY, x, y, linePaint);
            }

            previousX = x;
            previousY = y;
        }
    }
}

