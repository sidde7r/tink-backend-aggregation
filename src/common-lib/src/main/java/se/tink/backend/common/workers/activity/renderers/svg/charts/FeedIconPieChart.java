package se.tink.backend.common.workers.activity.renderers.svg.charts;

import java.awt.Color;

import se.tink.backend.common.workers.activity.renderers.svg.Canvas;

public class FeedIconPieChart extends FeedIcon {

    protected final int START_ANGLE = 90;
    protected final int FULL_SWEEP_ANGLE = 360;

    private double innerRadius;

    private Color currentAmountColor;
    private Color exceededAmountColor;
    private double filledPart;

    @Override
    protected void drawCircle(Canvas canvas)
    {

        float currentAmountSweepAngle = (float) (FULL_SWEEP_ANGLE * filledPart);
        Color activeColor;

        if (filledPart > 1d)
        {
            activeColor = getExceededAmountColor();
            currentAmountSweepAngle = FULL_SWEEP_ANGLE;
            canvas.drawCircle(getOuterRadius(), getOuterRadius(), getOuterRadius(), 0, activeColor);
        }
        else
        {
            super.drawCircle(canvas);
            activeColor = getCurrentAmountColor();
            canvas.drawCircleSector(getOuterRadius(), getOuterRadius(), innerRadius, 0, START_ANGLE,
                    currentAmountSweepAngle, activeColor);
        }

    }

    public double getInnerRadius() {
        return innerRadius;
    }

    public void setInnerRadius(double innerRadius) {
        this.innerRadius = innerRadius;
    }

    public Color getCurrentAmountColor() {
        return currentAmountColor;
    }

    public void setCurrentAmountColor(Color currentAmountColor) {
        this.currentAmountColor = currentAmountColor;
    }

    public void setFilledPart(double d) {
        filledPart = d;
    }

    public Color getExceededAmountColor() {
        return exceededAmountColor;
    }

    public void setExceededAmountColor(Color exceededAmountColor) {
        this.exceededAmountColor = exceededAmountColor;
    }

}
