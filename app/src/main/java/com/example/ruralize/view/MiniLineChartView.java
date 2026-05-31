package com.example.ruralize.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MiniLineChartView extends View {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Float> values = new ArrayList<>();
    private final Path linePath = new Path();
    private final Path fillPath = new Path();

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
        linePaint.setColor(0xFF2F5D39);
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setColor(0x333D7C4A);
        fillPaint.setStyle(Paint.Style.FILL);

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

        linePath.reset();
        fillPath.reset();

        float firstX = 0, firstY = 0, lastX = 0, lastY = 0;
        float previousX = 0, previousY = 0;

        for (int i = 0; i < values.size(); i++) {
            Float value = values.get(i);
            if (value == null) value = 0f;
            
            float x = i * stepX;
            float normalized = (value - min) / range;
            float y = height - (normalized * (height - 24)) - 12;

            if (i == 0) {
                linePath.moveTo(x, y);
                firstX = x; firstY = y;
            } else {
                float cpX = (previousX + x) / 2f;
                linePath.cubicTo(cpX, previousY, cpX, y, x, y);
            }

            previousX = x; previousY = y;
            lastX = x; lastY = y;
        }

        fillPath.addPath(linePath);
        fillPath.lineTo(lastX, height - 6);
        fillPath.lineTo(firstX, height - 6);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }
}
