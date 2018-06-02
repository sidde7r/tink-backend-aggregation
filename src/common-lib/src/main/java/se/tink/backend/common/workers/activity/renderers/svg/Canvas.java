package se.tink.backend.common.workers.activity.renderers.svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.util.List;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;
import se.tink.backend.common.utils.BezierSpline;
import se.tink.backend.common.workers.activity.renderers.svg.charts.Path;
import se.tink.backend.common.workers.activity.renderers.svg.charts.PointF;
import se.tink.backend.common.workers.activity.renderers.svg.charts.RectF;

public class Canvas {
    private final SVGGraphics2D svg;
    private final RectF bounds;

    public Canvas(int width, int height) {
        bounds = new RectF(new Rectangle(width, height));
        svg = new SVGGraphics2D(width, height);
    }

    public String draw() {
        return svg.getSVGElement();
    }

    public void drawText(String string, float x, float y, TextAnchor anchor, Paint paint, Font font) {
        svg.setFont(font);

        svg.setPaint(paint);

        TextUtilities.drawAlignedString(string, svg, x, y, anchor);
    }

    public void fillPath(int[] xPoints, int[] yPoints, Paint paint) {
        svg.setPaint(paint);
        svg.fillPolygon(xPoints, yPoints, Math.min(xPoints.length, yPoints.length));
    }

    public void drawFilledRect(RectF rectF, Paint rectPaint) {
        drawFilledRect((int) rectF.x, (int) rectF.y, (int) rectF.width, (int) rectF.height, rectPaint);
    }

    public void drawFilledRectTopRounded(RectF rectF, Paint rectPaint, int cornerRadius) {
        Area topLeftQuarterCircle = getQuarterCircleArea(rectF.x + cornerRadius, rectF.y + cornerRadius, cornerRadius,
                0, Corner.TOP_LEFT);
        Area topRightQuarterCircle = getQuarterCircleArea(rectF.x + rectF.width - cornerRadius, rectF.y + cornerRadius,
                cornerRadius, 0, Corner.TOP_RIGHT);

        Area topLeftBox = new Area(
                new RectF(new Rectangle((int) rectF.x, (int) rectF.y, cornerRadius, cornerRadius)));
        Area topRightBox = new Area(new RectF(new Rectangle((int) (rectF.x + rectF.width - cornerRadius),
                (int) rectF.y, cornerRadius, cornerRadius)));

        topLeftBox.subtract(topLeftQuarterCircle); // Inverted top left corner
        topRightBox.subtract(topRightQuarterCircle); // Inverted top right corner

        Area topRoundedRect = new Area(rectF);
        topRoundedRect.subtract(topLeftBox);
        topRoundedRect.subtract(topRightBox);

        svg.setPaint(rectPaint);
        svg.fill(topRoundedRect);
    }

    public void drawFilledRectRightRounded(RectF rectF, Paint rectPaint, int cornerRadius) {
        Area bottomRightQuarterCircle = getQuarterCircleArea(rectF.x + rectF.width - cornerRadius, rectF.y
                + rectF.height - cornerRadius, cornerRadius, 0, Corner.BOTTOM_RIGHT);
        Area topRightQuarterCircle = getQuarterCircleArea(rectF.x + rectF.width - cornerRadius, rectF.y + cornerRadius,
                cornerRadius, 0, Corner.TOP_RIGHT);

        Area bottomRightBox = new Area(new RectF(new Rectangle((int) (rectF.x + rectF.width - cornerRadius),
                (int) (rectF.y + rectF.height - cornerRadius), cornerRadius, cornerRadius)));
        Area topRightBox = new Area(new RectF(new Rectangle((int) (rectF.x + rectF.width - cornerRadius),
                (int) rectF.y, cornerRadius, cornerRadius)));

        bottomRightBox.subtract(bottomRightQuarterCircle); // Inverted top left corner
        topRightBox.subtract(topRightQuarterCircle); // Inverted top right corner

        Area topRoundedRect = new Area(rectF);
        topRoundedRect.subtract(bottomRightBox);
        topRoundedRect.subtract(topRightBox);

        svg.setPaint(rectPaint);
        svg.fill(topRoundedRect);
    }

    public void drawFilledRect(int x, int y, int width, int height, Paint paint) {
        svg.setPaint(paint);
        svg.fillRect(x, y, width, height);
    }

    public void drawGradientFilledRect(int x, int y, int width, int height, Color topColor, Color bottomColor) {
        topColor = whiteInsteadOfAlpha(topColor);
        bottomColor = whiteInsteadOfAlpha(bottomColor);

        LinearGradientPaint gradient = new LinearGradientPaint(0, height, 0, y, new float[] {
                0, 1
        }, new Color[] {
                bottomColor, topColor
        });
        drawFilledRect(x, y, width, height, gradient);
    }

    public void drawGradientFilledRect(RectF rectF, Color topColor, Color bottomColor) {
        drawGradientFilledRect((int) rectF.x, (int) rectF.y, (int) rectF.width, (int) rectF.height, topColor,
                bottomColor);
    }

    public void drawLinedPath(Path path, Stroke stroke, Paint paint, boolean smooth) {
        svg.setPaint(paint);
        svg.setStroke(stroke);

        if (smooth) {
            svg.draw(BezierSpline.getBezierSpline(path));
        } else {
            svg.drawPolygon(getPolygon(path));
        }
    }

    public void drawGradientFilledPath(Path path, Color topColor, Color bottomColor, boolean smooth) {
        if (path.getMinY() >= path.getMaxY()) {
            return;
        }

        svg.setPaint(createGradient(path, topColor, bottomColor));

        if (smooth) {
            svg.fill(BezierSpline.getBezierSpline(path));
        } else {
            svg.fillPolygon(getPolygon(path));
        }
    }

    private Paint createGradient(Path path, Color topColor, Color bottomColor) {
        topColor = whiteInsteadOfAlpha(topColor);
        bottomColor = whiteInsteadOfAlpha(bottomColor);

        return new LinearGradientPaint(0, path.getMaxY(), 0, path.getMinY(), new float[] {
                0, 1
        }, new Color[] {
                bottomColor, topColor
        });
    }

    private Polygon getPolygon(Path path) {
        List<PointF> points = path.getPoints();
        int[] xPoints = new int[points.size()];
        int[] yPoints = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            PointF point = points.get(i);
            xPoints[i] = (int) point.getX();
            yPoints[i] = (int) point.getY();
        }
        return new Polygon(xPoints, yPoints, xPoints.length);
    }

    public void drawFilledCircle(double origoX, double origoY, double radius, Color paint) {
        drawCircle(origoX, origoY, radius, 0, paint);
    }

    public void drawCircle(double origoX, double origoY, double outerRadius, double innerRadius, Color paint) {
        drawCircleSector(origoX, origoY, outerRadius, innerRadius, 0, 360, paint);
    }

    public void drawCircleSector(double origoX, double origoY, double outerRadius, double innerRadius,
            double startAngle, double extent, Color paint) {
    	paint = whiteInsteadOfAlpha(paint);

    	svg.setPaint(paint);

        Area a = getCircleSectorArea(origoX, origoY, outerRadius, innerRadius, startAngle, extent);

        svg.fill(a);
    }

    private Area getQuarterCircleArea(double origoX, double origoY, double outerRadius, double innerRadius,
            Corner corner) {
        double startAngle = 0;
        switch (corner) {
        case TOP_RIGHT:
            startAngle = 90;
            break;
        case BOTTOM_RIGHT:
            startAngle = 0;
            break;
        case BOTTOM_LEFT:
            startAngle = 270;
            break;
        case TOP_LEFT:
            startAngle = 180;
            break;
        }
        return getCircleSectorArea(origoX, origoY, outerRadius, innerRadius, startAngle, 90);
    }

    private Area getCircleSectorArea(double origoX, double origoY, double outerRadius, double innerRadius,
            double startAngle, double extent) {
        Arc2D.Double outerArc = new Arc2D.Double(origoX - outerRadius, origoY - outerRadius, outerRadius * 2,
                outerRadius * 2, startAngle, -extent, Arc2D.PIE);
        Arc2D.Double innerArc = new Arc2D.Double(origoX - innerRadius, origoY - innerRadius, innerRadius * 2,
                innerRadius * 2, startAngle, -extent, Arc2D.PIE);

        Area a = new Area(outerArc);
        a.subtract(new Area(innerArc));

        return a;
    }

    public RectF getBounds() {
        return bounds;
    }

    public void drawLine(float x1, float y1, float x2, float y2, Stroke stroke, Paint paint) {
        svg.setPaint(paint);
        svg.setStroke(stroke);

        svg.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    public int getHeight() {
        return (int) bounds.height;
    }

    public float getWidth() {
        return (int) bounds.width;
    }

    private enum Corner {
        TOP_RIGHT,
        TOP_LEFT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public int getTextWidth(String text, Font font) {
        return svg.getFontMetrics(font).stringWidth(text);
    }

    public int getTextHeight(String text, Font font) {
        return svg.getFontMetrics(font).getHeight();
    }

    private static Color whiteInsteadOfAlpha(Color color) {
        float aFactor = (float) (0xFF - color.getAlpha()) / (0xFF);
        return lighten(color, aFactor, false);
    }

    private static Color lighten(Color color, float factor, boolean includeAlpha) {
        float red = color.getRed();
        float green = color.getGreen();
        float blue = color.getBlue();

        red = red + (0xFF - red) * factor;
        green = green + (0xFF - green) * factor;
        blue = blue + (0xFF - blue) * factor;

        if (includeAlpha) {
            return new Color(color.getAlpha(), (int) red, (int) green, (int) blue);
        } else {
            return new Color((int) red, (int) green, (int) blue);
        }
    }
}
