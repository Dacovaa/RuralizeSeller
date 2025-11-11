package com.example.ruralize.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MiniBarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Float> values = new ArrayList<>();

    public MiniBarChartView(Context context) {
        super(context);
        init();
    }

    public MiniBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiniBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setColor(0xFF2F5D39);
        axisPaint.setColor(0x332F5D39);
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
        if (width <= 0 || height <= 0) return;

        canvas.drawLine(0, height - 6, width, height - 6, axisPaint);

        if (values.isEmpty()) return;

        float max = 0f;
        for (Float value : values) {
            if (value != null && value > max) {
                max = value;
            }
        }
        if (max <= 0f) {
            max = 1f;
        }

        float barWidth = width / (values.size() * 1.6f);
        float spacing = (width - (barWidth * values.size())) / (values.size() + 1);
        float offsetX = spacing;

        for (Float value : values) {
            float percent = value != null ? value / max : 0f;
            float barHeight = percent * (height - 12);
            float top = height - barHeight - 6;
            canvas.drawRect(offsetX, top, offsetX + barWidth, height - 6, barPaint);
            offsetX += barWidth + spacing;
        }
    }
}

