package se.tink.backend.common.workers.activity.renderers.svg.charts;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Currency;

public class LineChartArea extends ChartArea {
    
    private Color areaGradientBottomColor;
    private Color areaGradientTopColor;
    private Paint areaPaint;
    private boolean fill;
    private Color pathColor;
    private Stroke pathStroke;
    private List<PointF> points;
    private boolean smooth;

    public LineChartArea(Theme theme, Catalog catalog, Currency currency, Locale locale, Calendar calendar) {
        super(theme, catalog, currency, locale, calendar);

        setPathColor(getTheme().getColor(ColorTypes.PATH_COLOR));
        setPathStroke(new BasicStroke(2));
        setAreaGradientTopColor(getTheme().getColor(ColorTypes.PATH_COLOR, Theme.Alpha.P40));
        setAreaGradientBottomColor(getTheme().getColor(ColorTypes.PATH_COLOR, Theme.Alpha.P0));
    }

    public void draw(Canvas canvas, Boolean v2) {
        drawPath(canvas);
    }

    protected void drawPath(Canvas canvas) {
        Path path = getPath();
        canvas.drawLinedPath(path, pathStroke, pathColor, isSmooth());
    }

    public Color getAreaGradientBottomColor() {
        return areaGradientBottomColor;
    }

    public Color getAreaGradientTopColor() {
        return areaGradientTopColor;
    }

    public Paint getAreaPaint() {
        return areaPaint;
    }

    protected Path getPath() {
        return getPath(getPoints());
    }

    protected Path getPath(List<PointF> points) {
        Path path = new Path();

        if (points != null && points.size() > 0) {
            PointF firstPoint = points.get(0);
            path.moveTo(firstPoint.x, firstPoint.y);

            for (int i = 1; i < points.size(); i++) {
                PointF point = points.get(i);
                path.lineTo(point.x, point.y);
            }
        }
        
        return path;
    }

    protected List<BalancePath> getPath(List<PointF> points, final double threshold) {
        List<BalancePath> list = Lists.newArrayList();

        if (points == null) {
            return list;
        }

        BalancePath currentPath = newBalancePath();
        List<PointF> currentPoints = Lists.newArrayList();
        PointF currentXAxisInterSectionPoint = null;

        while (!points.isEmpty()) {
            int xAxisCrossedIndex;

            if (isPositivePoint(points.get(0), threshold)) {
                currentPath.aboveThreshold = true;
                xAxisCrossedIndex = Iterables.indexOf(points, point -> !isPositivePoint(point, threshold));
            } else {
                currentPath.aboveThreshold = false;
                xAxisCrossedIndex = Iterables.indexOf(points, point -> isPositivePoint(point, threshold));
            }

            // The graph never crosses the X-axis
            if (xAxisCrossedIndex == -1) {
                currentPoints.addAll(points);
                points.clear();
            } else {
                currentPoints.addAll(points.subList(0, xAxisCrossedIndex));
                if (points.size() > xAxisCrossedIndex) {
                    currentXAxisInterSectionPoint = getXAxisIntersectionPoint(points.get(xAxisCrossedIndex - 1),
                            points.get(xAxisCrossedIndex), threshold);
                    currentPoints.add(currentXAxisInterSectionPoint);
                }
                if (xAxisCrossedIndex + 1 <= points.size()) {
                    points = points.subList(xAxisCrossedIndex, points.size());
                } else {
                    points.clear();
                }
            }

            currentPath.path = getPath(currentPoints);
            currentPath.firstPoint = currentPoints.get(0);
            currentPath.lastPoint = currentPoints.get(currentPoints.size() - 1);
            list.add(currentPath);

            currentPath = newBalancePath();
            currentPoints.clear();

            // Add the intersection point to the next path too.
            if (currentXAxisInterSectionPoint != null) {
                currentPoints.add(currentXAxisInterSectionPoint);
                currentXAxisInterSectionPoint = null;
            }
        }

        return list;
    }

    public Color getPathPaint() {
        return pathColor;
    }

    protected Stroke getPathStroke() {
        return pathStroke;
    }

    public List<PointF> getPoints() {
        return Lists.newArrayList(points);
    }

    protected PointF getXAxisIntersectionPoint(PointF point1, PointF point2, double y0) {
        float dy1 = point2.y - point1.y; // Between points
        float dy2 = (float) y0 - point1.y; // Between first point and y0 (x-axis)
        float ratio = dy2 / dy1;

        float dx1 = point2.x - point1.x; // Between points
        float dx2 = dx1 * ratio; // Between first point and the intersection point

        return new PointF(point1.x + dx2, (float) y0);
    }

    public boolean isFill() {
        return fill;
    }

    protected boolean isPositivePoint(PointF point, double y0) {
        return point.y <= y0;
    }

    public boolean isSmooth() {
        return smooth;
    }

    private BalancePath newBalancePath() {
        BalancePath bp = new BalancePath();
        bp.path = new Path();
        return bp;
    }

    public void setAreaGradientBottomColor(Color areaGradientBottomColor) {
        this.areaGradientBottomColor = areaGradientBottomColor;
    }

    public void setAreaGradientTopColor(Color areaGradientTopColor) {
        this.areaGradientTopColor = areaGradientTopColor;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }

    public void setPathColor(Color color) {
        this.pathColor = color;
    }

    public void setPathStroke(Stroke stroke) {
        this.pathStroke = stroke;
    }

    public void setPoints(List<PointF> points) {
        this.points = points;
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }
}
