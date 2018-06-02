package se.tink.backend.common.workers.activity.renderers.svg.charts;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import org.joda.time.DateTime;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Balance;
import se.tink.backend.core.Currency;

public class BalanceLineChartArea extends AmountDateTimeLineChartArea {
    private List<Balance> balances;
    private Color negativeAreaGradientBottomColor;
    private Color negativeAreaGradientTopColor;
    private Color todayDotColor;
    private int todayDotDiameter = 4;

    public BalanceLineChartArea(Theme theme, Catalog catalog, Currency currency, Locale locale, Calendar calendar) {
        super(theme, catalog, currency, locale, calendar);
        
        setNegativeAreaGradientBottomColor(getTheme().getColor(ColorTypes.CRITICAL, Theme.Alpha.P40));
        setNegativeAreaGradientTopColor(getTheme().getColor(ColorTypes.CRITICAL, Theme.Alpha.P0));
        setTodayDotColor(getTheme().getColor(ColorTypes.DEFAULT));
    }

    @Override
    public void draw(Canvas canvas, Boolean v2) {
        if (getBalances() == null || getDates() == null) {
            return;
        }

        setPoints(getCurvePoints(getBalances(), getBounds(canvas)));

        super.draw(canvas, v2);

        if (isMarkToday()) {
            drawToday(canvas);
        }
    }

    @Override
    protected void drawArea(Canvas canvas) {
        List<PointF> points = getPoints();

        if (points.isEmpty()) {
            return;
        }

        float y0 = getYPoint(0, getBounds(canvas));
        List<BalancePath> paths = getPath(points, y0);

        for (BalancePath path : paths) {
            path.path.lineTo(path.lastPoint.x, y0);
            path.path.lineTo(path.firstPoint.x, y0);

            Color top = path.aboveThreshold ? getAreaGradientTopColor() : getNegativeAreaGradientTopColor();
            Color bottom = path.aboveThreshold ? getAreaGradientBottomColor() : getNegativeAreaGradientBottomColor();

            if (top != null && bottom != null) {
                canvas.drawGradientFilledPath(path.path, top, bottom, isSmooth());
            }
        }
    }

    @Override
    protected void drawPath(Canvas canvas) {
        List<PointF> points = getPoints();

        if (points.isEmpty()) {
            return;
        }

        float y0 = getYPoint(0, getBounds(canvas));
        List<BalancePath> paths = getPath(points, y0);

        // Draw all negative paths first, the graph will be unsmooth otherwise.
        for (BalancePath path : paths) {
            if (!path.aboveThreshold) {
                canvas.drawLinedPath(path.path, getPathStroke(), getNegativePathColor(), isSmooth());
            }
        }

        for (BalancePath path : paths) {
            if (path.aboveThreshold) {
                canvas.drawLinedPath(path.path, getPathStroke(), getPathPaint(), isSmooth());
            }
        }
    }

    protected void drawToday(Canvas canvas) {
        List<DateTime> dates = getDates();
        if (dates == null || dates.isEmpty()) {
            return;
        }
        
        List<PointF> points = getPoints();
        if (points == null || points.isEmpty()) {
            return;
        }
        
        List<Balance> balances = getBalances();
        if (balances == null || balances.isEmpty()) {
            return;
        }

        Balance lastBalance = balances.get(getBalances().size() - 1);
        PointF lastPoint = points.get(points.size() - 1);
        DateTime lastDate = dates.get(dates.size() - 1);

        Color color = null;
        
        if (I18NUtils.isToday(lastBalance.getDate(), getCalendar())) {
            color = getTodayDotColor();
        } else if (!I18NUtils.isSameDay(lastBalance.getDate(), lastDate)) {
            // Remove the part of the graph inside the circle
            color = (lastBalance.getAmount() >= 0 ? getPathPaint() : getNegativePathColor());
        } else {
            // Bail!
            return;
        }
        
        canvas.drawFilledCircle(lastPoint.x, lastPoint.y, getTodayDotDiameter(), color);
    }

    public List<Balance> getBalances() {
        return balances;
    }

    protected PointF getCurvePoint(Balance balance, RectF bounds) {
        return new PointF(getXPoint(balance.getDate(), bounds), getYPoint(balance.getAmount(), bounds));
    }

    protected List<PointF> getCurvePoints(List<Balance> balances, RectF bounds) {
        List<PointF> points = Lists.newArrayList();
        
        if (balances != null) {
            for (Balance balance : balances) {
                points.add(getCurvePoint(balance, bounds));
            }
        }
        
        return points;
    }

    public Color getNegativeAreaGradientBottomColor() {
        return negativeAreaGradientBottomColor;
    }

    public Color getNegativeAreaGradientTopColor() {
        return negativeAreaGradientTopColor;
    }

    public Color getTodayDotColor() {
        return todayDotColor;
    }
    
    public int getTodayDotDiameter() {
        return todayDotDiameter;
    }

    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }

    public void setNegativeAreaGradientBottomColor(Color color) {
        this.negativeAreaGradientBottomColor = color;
    }

    public void setNegativeAreaGradientTopColor(Color color) {
        this.negativeAreaGradientTopColor = color;
    }

    private void setTodayDotColor(Color color) {
        this.todayDotColor = color;
    }

    public void setTodayDotDiameter(int diameter) {
        this.todayDotDiameter = diameter;
    }
}
