package se.tink.backend.common.workers.activity.renderers.svg.charts;

import java.awt.Color;

import org.jfree.ui.TextAnchor;

import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.utils.FontUtils.Fonts;

public class FeedIcon {

    private Color backgroundColor;
    private double radius;
    private char icon;
    private Color iconColor;
    private float iconSize;

    public void draw(Canvas canvas, boolean v2)
    {
        drawCircle(canvas);
        makeLabels(canvas, v2);
    }

    protected void drawCircle(Canvas canvas)
    {
        canvas.drawCircle(radius, radius, radius, 0, backgroundColor);
    }

    private void makeLabels(Canvas canvas, boolean v2)
    {
        TextAnchor anchor;

        float x = (float) getOuterRadius();
        float y = (float) getOuterRadius();
        float iconSize = getIconSize();

        if (v2) {
            anchor = TextAnchor.CENTER_RIGHT;
            x = x - 4;
            y = y + 2;
            iconSize = iconSize - 6;
        } else {
            anchor = TextAnchor.CENTER;
        }

        canvas.drawText(String.valueOf(icon), x, y, anchor, iconColor,
                Fonts.TINK_ICONS.deriveFont(iconSize));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public double getOuterRadius() {
        return radius;
    }

    public void setRadius(double outerRadius) {
        this.radius = outerRadius;
    }

    public char getIcon() {
        return icon;
    }

    public void setIcon(char icon) {
        this.icon = icon;
    }

    public Color getIconColor() {
        return iconColor;
    }

    public void setIconColor(Color iconColor) {
        this.iconColor = iconColor;
    }

    public float getIconSize() {
        return iconSize;
    }

    public void setIconSize(float iconSize) {
        this.iconSize = iconSize;
    }
}
