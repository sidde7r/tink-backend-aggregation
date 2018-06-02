package se.tink.backend.common.workers.activity.renderers.svg.charts;

import com.google.common.base.Objects;
import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Calendar;
import java.util.Locale;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Currency;

public class ChartArea {

    public enum XAxisLabelPosition {
        BOTTOM_INSIDE_FOR_POSITIVE_OUTSIDE_FOR_NEGATIVE,
        BOTTOM_OUTSIDE_CHARTAREA,
        NONE,
    }

    public enum YAxisLabelPosition {
        LEFT_INSIDE_CHARTAREA,
        LEFT_INSIDE_CHARTAREA_ABOVE_GRIDLINE,
        LEFT_OUTSIDE_CHARTAREA,
        NONE,
        RIGHT_INSIDE_CHARTAREA,
        RIGHT_OUTSIDE_CHARTAREA
    }

    private final Calendar calendar;
    private final Catalog catalog;
    private final Currency currency;
    private final Locale locale;
    private final Theme theme;

    private Paint axisPaint;

    // X axis
    private boolean highlightXAxis;
    private boolean showXAxisGridlines;
    private XAxisLabelPosition xAxisLabelPosition;
    private Paint xAxisGridlinePaint;
    private Stroke xAxisGridlineStroke;
    private Paint xAxisPaint;
    private Stroke xAxisStroke = new BasicStroke(1);
    private Paint xAxisLabelPaint;
    private float xAxisLabelTextSize = 12;

    // Y axis
    private boolean showYAxisGridlines;
    private YAxisLabelPosition yAxisLabelPosition;
    private Paint yAxisGridlinePaint;
    private Stroke yAxisGridlineStroke;
    private int yAxisLabelMaxCount = 3;
    private Paint yAxisLabelPaint;
    private int yAxisLabelSideMargin = 6;
    private float yAxisLabelTextSize = 12;

    public ChartArea(Theme theme, Catalog catalog, Currency currency, Locale locale, Calendar calendar) {
        this.theme = theme;
        this.catalog = catalog;
        this.currency = currency;
        this.locale = locale;
        this.calendar = calendar;
        // Common
        setAxisPaint(getTheme().getColor(ColorTypes.CHART_AXIS));

        // X axis
        setXAxisGridlinePaint(getAxisPaint());
        setXAxisLabelPaint(getTheme().getColor(ColorTypes.CHART_AXIS_X_LABEL));
        setXAxisPaint(getTheme().getColor(ColorTypes.WARNING));

        // Y axis
        setYAxisGridlinePaint(getAxisPaint());
        setYAxisLabelPaint(getTheme().getColor(ColorTypes.CHART_AXIS_Y_LABEL));
    }

    protected RectF addHorizontalPadding(RectF rectangle) {
        RectF paddedRectangle = new RectF(rectangle);

        paddedRectangle.x += getLeftPadding();
        paddedRectangle.width -= getRightPadding() + getLeftPadding();

        return paddedRectangle;
    }

    protected RectF addPadding(RectF rectangle) {
        RectF paddedRectangle = new RectF(rectangle);

        paddedRectangle.x += getLeftPadding();
        paddedRectangle.width -= getRightPadding() + getLeftPadding();

        paddedRectangle.y += getTopPadding();
        paddedRectangle.height -= getBottomPadding() + getTopPadding();

        return paddedRectangle;
    }

    public Paint getAxisPaint() {
        return axisPaint;
    }

    protected float getBottomPadding() {
        return getVerticalPadding();
    }

    protected RectF getBounds(Canvas canvas) {
        return addPadding(canvas.getBounds());
    }

    protected Calendar getCalendar() {
        return calendar;
    }

    protected Catalog getCatalog() {
        return catalog;
    }

    protected Currency getCurrency() {
        return currency;
    }

    public String getFormattedAmountLabel(double amount) {
        return I18NUtils.formatCurrency(amount, getCurrency(), getLocale(), theme.getYAxisCurrencyFormat());
    }

    protected int getHeight(Canvas canvas) {
        return canvas.getHeight();
    }

    protected float getHorizontalPadding() {
        return 0;
    }

    protected float getLeftPadding() {
        return getHorizontalPadding();
    }

    protected Locale getLocale() {
        return locale;
    }

    protected float getRightPadding() {
        return getHorizontalPadding();
    }

    protected Theme getTheme() {
        return theme;
    }

    protected float getTopPadding() {
        return getVerticalPadding();
    }

    protected float getVerticalPadding() {
        return 0;
    }

    protected float getWidth(Canvas canvas) {
        return canvas.getWidth();
    }

    public Paint getXAxisGridlinePaint() {
        return xAxisGridlinePaint;
    }

    public Stroke getXAxisGridlineStroke() {
        return xAxisGridlineStroke;
    }

    public Paint getXAxisLabelPaint() {
        return xAxisLabelPaint;
    }

    public XAxisLabelPosition getXAxisLabelPosition() {
        return xAxisLabelPosition;
    }

    protected float getXAxisLabelTextSize() {
        return xAxisLabelTextSize;
    }

    public Paint getXAxisPaint() {
        return xAxisPaint;
    }

    public Stroke getXAxisStroke() {
        return xAxisStroke;
    }

    public Paint getYAxisGridlinePaint() {
        return yAxisGridlinePaint;
    }

    public Stroke getYAxisGridlineStroke() {
        return yAxisGridlineStroke;
    }

    public Paint getYAxisLabelPaint() {
        return yAxisLabelPaint;
    }

    public YAxisLabelPosition getYAxisLabelPosition() {
        return yAxisLabelPosition;
    }

    public int getYAxisLabelsMaxCount() {
        return yAxisLabelMaxCount;
    }

    protected int getYAxisLabelsSideMargin() {
        return yAxisLabelSideMargin;
    }

    public float getYAxisLabelTextSize() {
        return yAxisLabelTextSize;
    }

    public boolean isHighlightXAxis() {
        return highlightXAxis;
    }

    public boolean isShowXAxisGridlines() {
        return showXAxisGridlines;
    }

    public boolean isShowYAxisGridlines() {
        return showYAxisGridlines;
    }

    private void setAxisPaint(Paint paint) {
        this.axisPaint = paint;
    }

    public void setHighlightXAxis(boolean highlight) {
        this.highlightXAxis = highlight;
    }

    public void setShowXAxisGridlines(boolean showGridlines) {
        this.showXAxisGridlines = showGridlines;
    }

    public void setShowYAxisGridlines(boolean showGridlines) {
        this.showYAxisGridlines = showGridlines;
    }

    public void setXAxisGridlinePaint(Paint paint) {
        this.xAxisGridlinePaint = paint;
    }

    public void setXAxisGridlineStroke(Stroke stroke) {
        this.xAxisGridlineStroke = stroke;
    }

    public void setXAxisLabelPaint(Paint paint) {
        this.xAxisLabelPaint = paint;
    }

    public void setXAxisLabelPosition(XAxisLabelPosition position) {
        this.xAxisLabelPosition = position;
    }

    public void setXAxisLabelTextSize(float textSize) {
        this.xAxisLabelTextSize = textSize;
    }

    public void setXAxisPaint(Paint paint) {
        this.xAxisPaint = paint;
    }

    public void setXAxisStroke(Stroke stroke) {
        this.xAxisStroke = stroke;
    }

    public void setYAxisGridlinePaint(Paint paint) {
        this.yAxisGridlinePaint = paint;
    }

    public void setYAxisGridlineStroke(Stroke stroke) {
        this.yAxisGridlineStroke = stroke;
    }

    public void setYAxisLabelMaxCount(int count) {
        this.yAxisLabelMaxCount = count;

        if (yAxisLabelMaxCount > 0 && Objects.equal(YAxisLabelPosition.NONE, getYAxisLabelPosition())) {
            setYAxisLabelPosition(YAxisLabelPosition.LEFT_OUTSIDE_CHARTAREA);
        }
    }

    public void setYAxisLabelPaint(Paint paint) {
        this.yAxisLabelPaint = paint;
    }

    public void setYAxisLabelPosition(YAxisLabelPosition position) {
        this.yAxisLabelPosition = position;
    }

    public void setYAxisLabelSideMargin(int margin) {
        this.yAxisLabelSideMargin = margin;
    }

    public void setYAxisLabelTextSize(float textSize) {
        this.yAxisLabelTextSize = textSize;
    }
}
