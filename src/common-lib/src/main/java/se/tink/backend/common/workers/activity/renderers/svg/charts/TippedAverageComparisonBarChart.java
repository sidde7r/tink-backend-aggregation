package se.tink.backend.common.workers.activity.renderers.svg.charts;

import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.Calendar;
import java.util.Locale;
import org.jfree.ui.TextAnchor;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Currency;
import se.tink.backend.core.TinkUserAgent;

public class TippedAverageComparisonBarChart extends ChartArea {
    
    private final float barWidth = 20;
    private final Font categoryFont;
    private final Font font;
    private final int labelMargin = 8;
    private final float labelSize = 10;
    private final float titleMargin = 16;
    private final float titleSize = 10;
    
    private String amountLabel;
    private Paint avgRectPaint;
    private float avgValue;
    private Paint currentRectPaint;
    private float currentValue;
    private double maxValue;
    private String title;

    public TippedAverageComparisonBarChart(Theme theme, Catalog catalog, Currency currency, Locale locale,
            Calendar calendar, TinkUserAgent userAgent) {
        super(theme, catalog, currency, locale, calendar);
        
        this.categoryFont = theme.getLightFont().deriveFont(titleSize);
        this.font = theme.getRegularFont().deriveFont(titleSize);
    }

    public void draw(Canvas canvas) {
        drawRects(canvas);
    }

    protected void drawRects(Canvas canvas) {
        RectF rect = addPadding(canvas.getBounds());

        float startX = rect.x;
        float avgX = getBarWidth(avgValue, rect);
        float currentX = getBarWidth(currentValue, rect);

        float avgYbottom = rect.y + rect.height - barWidth;
        float avgYHeight = barWidth;
        float currentYBottom = avgYbottom - getBarMargin();
        float currentYHeight = barWidth;

        RectF avgRect = new RectF(new Rectangle((int) startX, (int) avgYbottom, (int) avgX, (int) avgYHeight));
        RectF currentRect = new RectF(new Rectangle((int) startX, (int) currentYBottom, (int) currentX,
                (int) currentYHeight));

        canvas.drawFilledRectRightRounded(avgRect, getTheme().getColor(ColorTypes.EXPENSES_COMPARISON), 2);
        canvas.drawFilledRectRightRounded(currentRect, getTheme().getColor(ColorTypes.EXPENSES), 2);

        float textX = (int) (currentRect.x + currentRect.width - labelMargin);
        int textWidth = canvas.getTextWidth(getAmountLabel(), font);
        float textY = (int) (currentRect.y + currentRect.height / 2 + labelSize * 0.34);
        String amountLabel = getAmountLabel();

        // Text fits inside bar
        if (textX - rect.x >= textWidth) {
            canvas.drawText(amountLabel, textX, textY, TextAnchor.BASELINE_RIGHT, Theme.Colors.WHITE, font);
        } else {
            float adjustedTextX = textX + labelMargin * 2;
            canvas.drawText(amountLabel, adjustedTextX, textY, TextAnchor.BASELINE_LEFT,
                    getTheme().getColor(ColorTypes.EXPENSES), font);
        }

        canvas.drawText(getTitle(), rect.x + 6, (avgYbottom - titleMargin), TextAnchor.BASELINE_LEFT,
                Theme.Colors.BLACK, categoryFont);
    }

    public String getAmountLabel() {
        return amountLabel;
    }

    public Paint getAvgRectPaint() {
        return avgRectPaint;
    }

    public float getAvgValue() {
        return avgValue;
    }

    protected float getBarMargin() {
        return 10;
    }

    protected float getBarWidth(double value, RectF bounds) {
        return (float) (bounds.width * (value / getMaxValue()));
    }

    public Paint getCurrentRectPaint() {
        return currentRectPaint;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public String getTitle() {
        return title;
    }

    @Override
    protected float getTopPadding() {
        return 0;
    }

    public void setAmountLabel(String text) {
        this.amountLabel = text;
    }

    public void setAvgRectPaint(Paint paint) {
        this.avgRectPaint = paint;
    }

    public void setAvgValue(float value) {
        this.avgValue = value;
    }

    public void setCurrentRectPaint(Paint paint) {
        this.currentRectPaint = paint;
    }

    public void setCurrentValue(float value) {
        this.currentValue = value;
    }

    public void setMaxValue(double value) {
        this.maxValue = value;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
