package se.tink.backend.common.workers.activity.renderers.svg.charts;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import org.jfree.ui.TextAnchor;
import org.joda.time.DateTime;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Currency;
import se.tink.backend.utils.ChartUtils;
import se.tink.backend.utils.ChartUtils.PeriodType;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;

public class AmountDateTimeLineChartArea extends LineChartArea {
    
    private Color dateMarkerCenterColor;
    private List<DateTime> dates;
    private boolean makeRoomForMarkToday;
    private DateTime markDate;
    private Paint markedDateTextFillerPaint;
    private Paint markedDateTextPaint;
    private Paint markedDateTickPaint;
    private boolean markToday;
    private double maxValue;
    private double minValue;
    private Color negativePathColor;
    private boolean showXAxis;
    private boolean drawXAxisOnTop;
    private boolean yAxisLabelIncludeZeroLine = false;

    public AmountDateTimeLineChartArea(Theme theme, Catalog catalog, Currency currency, Locale locale, Calendar calendar) {
        super(theme, catalog, currency, locale, calendar);
        
        setDateMarkerCenterColor(Theme.Colors.WHITE);
        setMarkedDateTextFillerPaint(Theme.getColor(Theme.Colors.WHITE, Theme.Alpha.P80));
        setMarkedDateTextPaint(Theme.Colors.BLACK);
        setMarkedDateTickPaint(Theme.getColor(Theme.Colors.WHITE, Theme.Alpha.P80));
        setNegativePathColor(getTheme().getColor(ColorTypes.CRITICAL));
    }
    
    @Override
    public void draw(Canvas canvas, Boolean v2) {
        List<Double> yLines = ChartUtils
                .getGuidelines(minValue, maxValue, getYAxisLabelsMaxCount(), yAxisLabelIncludeZeroLine);

        if (isShowXAxis() && !drawXAxisOnTop) {
            drawXAxis(canvas);
        }

        if (isShowXAxisGridlines()) {
            drawXAxisGridlines(canvas);
        }

        if (isShowYAxisGridlines()) {
            drawYAxisGridlines(canvas, yLines);
        }

        if (isFill()) {
            drawArea(canvas);
        }

        if (isShowXAxis() && drawXAxisOnTop) {
            drawXAxis(canvas);
        }

        super.draw(canvas, v2);

        if (!Objects.equal(YAxisLabelPosition.NONE, getYAxisLabelPosition())) {
            drawYAxisLabels(canvas, yLines);
        }

        if (!Objects.equal(XAxisLabelPosition.NONE, getXAxisLabelPosition()) || isShowXAxisGridlines()) {
            drawXAxisLabels(canvas);
        }
    }
    
    protected void drawArea(Canvas canvas) {
        // Do nothing.
    }
    
    protected void drawXAxis(Canvas canvas) {
        RectF bounds = getBounds(canvas);
        
        float x1 = bounds.x;
        float x2 = (float) bounds.getWidth() - bounds.x;
        float y0 = getYPoint(0, bounds);

        canvas.drawLine(x1, y0, x2, y0, getXAxisStroke(), getXAxisPaint());
    }

    protected void drawXAxisGridlines(Canvas canvas) {
        DateTime firstDate = dates.get(0);
        DateTime lastDate = dates.get(dates.size() - 1);
        
        int daysApart = DateUtils.daysBetween(firstDate, lastDate);
        float rightMarginPercentage = (getLastLabelMarginNeeded()) / (getWidth(canvas)); // margin for rightmost label
        DateTime lastDrawableDate = lastDate.plusDays(-(int) Math.ceil(rightMarginPercentage * daysApart));
        PeriodType periodType = ChartUtils.getStepPeriodType(firstDate, lastDrawableDate, getCalendar());

        DateTime currentLabelDate = ChartUtils.getFirstLabelDate(firstDate, periodType, getCalendar());
        
        while (currentLabelDate.isBefore(lastDrawableDate)) {
            drawXAxisGridlines(currentLabelDate, canvas);
            currentLabelDate = ChartUtils.nextLabelDate(currentLabelDate, periodType, getCalendar());
        }
    }

    protected void drawXAxisGridlines(DateTime date, Canvas canvas) {
        if (isShowXAxisGridlines()) {
            RectF bounds = getBounds(canvas);
            float x = getXPoint(date, bounds);
            float y1 = getYPoint(Math.min(0, minValue), bounds);
            float y2 = bounds.y;
            
            canvas.drawLine(x, y1, x, y2, getPathStroke(), getAxisPaint());
        }
    }

    protected void drawXAxisLabels(Canvas canvas) {
        List<DateTime> dates = getDates();

        if (dates.isEmpty()) {
            return;
        }

        DateTime firstDate = dates.get(0);
        DateTime lastDate = dates.get(dates.size() - 1);

        int daysApart = DateUtils.getNumberOfDaysBetween(firstDate, lastDate);
        float rightMarginPercentage = (getLastLabelMarginNeeded()) / (getWidth(canvas)); // margin for rightmost label
        DateTime lastDrawableDate = lastDate.plusDays(-(int) Math.ceil(rightMarginPercentage * daysApart));
        PeriodType periodType = ChartUtils.getStepPeriodType(firstDate, lastDrawableDate, getCalendar());
        
        DateTime currentLabelDate = ChartUtils.getFirstLabelDate(firstDate, periodType, getCalendar());
        
        while (currentLabelDate.isBefore(lastDrawableDate)) {
            drawXLabel(currentLabelDate, daysApart, canvas);
            currentLabelDate = ChartUtils.nextLabelDate(currentLabelDate, periodType, getCalendar());
        }
    }

    protected void drawXLabel(DateTime date, int daysInGraph, Canvas canvas) {
        
        if (getXAxisLabelPosition() == null) {
            return;
        }
        
        RectF bounds = getBounds(canvas);
        
        float x;
        float y;
                
        switch(getXAxisLabelPosition()) {
        case BOTTOM_OUTSIDE_CHARTAREA:
            x = getXPoint(date, bounds) + 2;
            y = getHeight(canvas) - getXLabelBottomMargin();
            break;
        case BOTTOM_INSIDE_FOR_POSITIVE_OUTSIDE_FOR_NEGATIVE:
            x = getXPoint(date, bounds) + 8;
            y = getHeight(canvas) - (5 + getXLabelBottomMargin());
            break;
        default:
            // Don't draw anything. Bail!
            return;
        }

        String text = getTheme().formatDate(getCatalog(), getLocale(), date);

        Font font = getTheme().getLightFont().deriveFont(12f);
        
        canvas.drawText(text, x, y, TextAnchor.BASELINE_LEFT, getXAxisLabelPaint(), font);
    }

    protected void drawYAxisGridlines(Canvas canvas, List<Double> lines) {
        for (Double line : lines) {
            drawYTick(canvas, line);
        }
    }

    protected void drawYAxisLabels(Canvas canvas, List<Double> lines) {
        for (int i = 0; i < lines.size(); i++) {
            drawYLabel(canvas, lines.get(i));
        }
    }

    protected void drawYLabel(Canvas canvas, double value) {
        if (Objects.equal(YAxisLabelPosition.NONE, getYAxisLabelPosition())) {
            return;
        }

        RectF bounds = getBounds(canvas);
        float y = getYPoint(value, bounds);

        if (Objects.equal(YAxisLabelPosition.LEFT_INSIDE_CHARTAREA_ABOVE_GRIDLINE, getYAxisLabelPosition())) {
            // Above gridline
            y -= getYAxisLabelTextSize() * 0.7f;
        } else {
            // Below gridline
            y += getYAxisLabelTextSize() * 0.7f;
        }
        
        float x = getYAxisLabelsSideMargin();
        
        Font font = getTheme().getLightFont().deriveFont(getYAxisLabelTextSize());
        
        canvas.drawText(getFormattedAmountLabel(value), x, y, TextAnchor.CENTER_LEFT, getYAxisLabelPaint(), font);
    }

    private void drawYTick(Canvas canvas, double amount) {
        RectF bounds = getBounds(canvas);
        float y = getYPoint(amount, bounds);

        if (isShowYAxisGridlines()) {
            Paint paint;
            if (amount == 0 && isHighlightXAxis()) {
                paint = getXAxisPaint();
            } else {
                paint = getYAxisGridlinePaint();
            }
            
            float x1 = 0;
            float x2 = bounds.x + bounds.width;
            
            if (YAxisLabelPosition.LEFT_OUTSIDE_CHARTAREA == getYAxisLabelPosition()) {
                x1 = getYAxisLabelsSideMargin() + 30;
            }
            
            canvas.drawLine(x1, y, x2, y, getYAxisGridlineStroke(), paint);
        }
    }

    @Override
    protected float getBottomPadding() {
        float padding = 0;

        if (markToday) {
            padding += 5;
        } else if (minValue < 0) {
            padding += 1; // Make at least room for the stroke
        }

        if (getMakeRoomForXLabels()) {
            padding += getXAxisLabelTextSize() + (getXLabelBottomMargin() * 2);
        }

        return padding;
    }

    public Color getDateMarkerCenterColor() {
        return dateMarkerCenterColor;
    }

    public List<DateTime> getDates() {
        return dates;
    }

    public int getIndex(DateTime date) {
        List<DateTime> dates = getDates();
        if (dates == null || dates.size() == 0) {
            return -1;
        }

        int i;
        for (i = 0; i < dates.size(); i++) {
            if (DateUtils.isSameDay(date.toDate(), dates.get(i).toDate())) {
                return i;
            }
        }

        return -1;
    }

    protected int getLastLabelMarginNeeded() {
        return 31;
    }

    public boolean getMakeRoomForXLabels() {
        switch (getXAxisLabelPosition()) {
        case BOTTOM_OUTSIDE_CHARTAREA:
            return true;
        case BOTTOM_INSIDE_FOR_POSITIVE_OUTSIDE_FOR_NEGATIVE:
            if (getMinValue() < 0) {
                return true;
            } else {
                return false;
            }
        default:
            return false;
        }
    }

    public DateTime getMarkDate() {
        return markDate;
    }

    public Paint getMarkedDateTextPaint() {
        return markedDateTextPaint;
    }

    public Paint getMarkedDateTickPaint() {
        return markedDateTickPaint;
    }

    public Paint getMarkedTextFillerPaint() {
        return markedDateTextFillerPaint;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public Color getNegativePathColor() {
        return negativePathColor;
    }

    @Override
    protected float getTopPadding() {
        float padding = 0;

        if (markToday || makeRoomForMarkToday) {
            padding += 5;
        } else if (maxValue >= 0) {
            padding += 1; // Make at least room for the stroke
        }

        return padding;
    }

    protected float getXLabelBottomMargin() {
        switch (getXAxisLabelPosition()) {
        case BOTTOM_OUTSIDE_CHARTAREA:
        case BOTTOM_INSIDE_FOR_POSITIVE_OUTSIDE_FOR_NEGATIVE:
            return 10;
        default:
            return 0;
        }
    }

    protected float getXPoint(final DateTime date, RectF bounds) {
        int index = Iterables.indexOf(getDates(), d -> DateUtils.isSameDay(date.toDate(), d.toDate()));

        return bounds.x + bounds.width * index / (getDates().size() - 1);
    }

    protected float getYPoint(double value, RectF bounds) {
        return (float) (bounds.y + bounds.getHeight() * (1 - (value - minValue) / (maxValue - minValue)));
    }

    public boolean isMakeRoomForMarkToday() {
        return makeRoomForMarkToday;
    }

    public boolean isMarkToday() {
        return markToday;
    }

    public boolean isShowXAxis() {
        return showXAxis;
    }

    public void setDateMarkerCenterColor(Color dateMarkerCenterColor) {
        this.dateMarkerCenterColor = dateMarkerCenterColor;
    }

    public void setDates(List<DateTime> dates) {
        this.dates = dates;
    }

    public void setMakeRoomForMarkToday(boolean makeRoomForMarkToday) {
        this.makeRoomForMarkToday = makeRoomForMarkToday;
    }

    public void setMarkDate(DateTime markDate) {
        this.markDate = markDate;
    }

    private void setMarkedDateTextFillerPaint(Paint paint) {
        this.markedDateTextFillerPaint = paint;
    }

    private void setMarkedDateTextPaint(Paint paint) {
        this.markedDateTextPaint = paint;
    }

    private void setMarkedDateTickPaint(Paint paint) {
        this.markedDateTickPaint = paint;
    }

    public void setMarkToday(boolean markToday) {
        this.markToday = markToday;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public void setNegativePathColor(Color color) {
        this.negativePathColor = color;
    }

    public void setShowXAxis(boolean showXAxis) {
        this.showXAxis = showXAxis;
    }

    public void setDrawXAxisOnTop(boolean drawXAxisLast) {
        this.drawXAxisOnTop = drawXAxisLast;
    }

    public void setYAxisLabelIncludeZeroLine(boolean yAxisLabelIncludeZeroLine) {
        this.yAxisLabelIncludeZeroLine = yAxisLabelIncludeZeroLine;
    }

}
