package se.tink.backend.common.workers.activity.renderers.svg.charts;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.List;
import org.jfree.ui.TextAnchor;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.themes.SectionColor;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.TinkUserAgent;

public class CategorizedGraph {

    private double categorizationLevel;

    private float textSize;
    private String text;

    private final Theme theme;
    private TinkUserAgent userAgent;

    public CategorizedGraph(Theme theme, TinkUserAgent userAgent) {
        this.theme = theme;
        this.userAgent = userAgent;
    }

    public void draw(Canvas canvas) {
        drawBackgroundGraph(canvas);
        drawProgress(canvas);
    }

    private void drawBackgroundGraph(Canvas canvas) {
        List<SectionColor> sections = theme.getCategorizationProgressBackgroundSections();
        for (SectionColor section : sections) {
            drawBar(section.getFrom(), section.getTo(), section.getColor(), canvas);
        }
    }

    private void drawProgress(Canvas canvas) {
        Color progressColor = theme.getCategorizationProgressColor(categorizationLevel);
        setDoneText(canvas, progressColor);
        drawBar(0, categorizationLevel, progressColor, canvas);
    }

    private void setDoneText(Canvas canvas, Paint textPaint) {

        float x = (float) Math.round(getX(categorizationLevel, canvas));
        float y = (float) (getBottom(canvas) - getGraphStrokeWidth(canvas) - textSize / 2);

        canvas.drawText(text, x, y, TextAnchor.BASELINE_RIGHT, textPaint,
                theme.getBoldFont().deriveFont(textSize));
    }

    public double getCategorizationLevel() {
        return categorizationLevel;
    }

    public void setCategorizationLevel(double categorizationLevel) {
        this.categorizationLevel = categorizationLevel;
    }

    private void drawBar(double fromPercent, double toPercent, Paint paint, Canvas canvas) {
        canvas.drawFilledRect(new RectF(new Rectangle((int) getX(fromPercent, canvas),
                (int) (getBottom(canvas) - getGraphStrokeWidth(canvas)), (int) getX(toPercent, canvas),
                (int) getBottom(canvas))), paint);
    }

    private double getX(double percent, Canvas canvas) {
        return canvas.getWidth() * percent;
    }

    private double getBottom(Canvas canvas) {
        return canvas.getHeight();
    }

    private double getGraphStrokeWidth(Canvas canvas) {
        return 5;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public void setText(String text) {
        this.text = text;
    }

}
