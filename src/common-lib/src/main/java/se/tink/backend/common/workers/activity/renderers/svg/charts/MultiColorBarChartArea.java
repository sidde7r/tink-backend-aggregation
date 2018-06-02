package se.tink.backend.common.workers.activity.renderers.svg.charts;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import org.jfree.ui.TextAnchor;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.I18NUtils.CurrencyFormat;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Currency;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.utils.ChartUtils;

public class MultiColorBarChartArea extends ChartArea {

    private final TinkUserAgent userAgent;
    private float additionalTopPadding;
    private float amountLabelTextSize;
    private List<MultiColorBarData> barData;
    private float barMargin = 10;
    private int barMinHeight;
    private int cornerRadius = 4;
    private Paint gridlinePaint;
    private float leftMargin;
    private double maxValue;
    private double minValue;
    private Paint rectPaint;

    public MultiColorBarChartArea(Theme theme, Catalog catalog, Currency currency, Locale locale, Calendar calendar,
            TinkUserAgent userAgent) {
        super(theme, catalog, currency, locale, calendar);
        this.userAgent = userAgent;
    }

    public void draw(Canvas canvas) {
        drawYAxisGridlines(canvas);
        drawBars(canvas);
    }

    private void drawAmountLabels(List<ColorRect> rects, Canvas canvas) {
        RectF labelArea = addHorizontalPadding(canvas.getBounds());
        drawAmountLabels(rects, canvas, labelArea);
    }

    protected void drawAmountLabels(List<ColorRect> rects, Canvas canvas, RectF labelArea) {
        final Font font = getTheme().getBoldFont().deriveFont(getAmountLabelTextSize());

        int barCount = Math.min(rects.size(), getBarMaxCount());
        float barWidth = getBarWidth(barCount, labelArea);

        float x = labelArea.x + leftMargin + barWidth / 2;

        for (ColorRect rect : rects) {

            String text = rect.getLabel();

            if (text != null && rect.getLabelColor() != null) {
                float y = (rect.getRectF().y - (getAmountLabelTextSize() / 2));
                canvas.drawText(text, x, y, TextAnchor.BASELINE_CENTER, rect.getLabelColor(), font);
            }

            x += barWidth + getBarMargin();
        }
    }

    private void drawBars(Canvas canvas) {
        if (getBarData() == null) {
            return;
        }

        List<ColorRect> rects = getRects(getBarData(), getBounds(canvas));
        drawRects(canvas, rects);
        drawLabels(getBarData(), rects, canvas);
        drawAmountLabels(rects, canvas);
    }

    protected void drawLabels(List<MultiColorBarData> dataPoints, Canvas canvas, RectF labelArea) {
        int barCount = Math.min(dataPoints.size(), getBarMaxCount());

        float barWidth = getBarWidth(barCount, labelArea);
        float x = labelArea.x + leftMargin + barWidth / 2;
        float y = 0;

        for (MultiColorBarData dataPoint : dataPoints) {
            for (int i = 0; i < getValidRectanglesCount(dataPoint); i++) {
                String text = dataPoint.getLabel();
                setXAxisLabelPaint(dataPoint.getLabelColor());

                Font font;

                if (dataPoint.isLabelBold()) {
                    font = getTheme().getBoldFont().deriveFont(getXAxisLabelTextSize());
                } else {
                    font = getTheme().getLightFont().deriveFont(getXAxisLabelTextSize());
                }

                float size = getXAxisLabelTextSize();

                if (text != null) {
                    if (text.contains("\n")) {
                        String[] strings = text.split("\n");

                        y = (float) (labelArea.getHeight() - (1.75 * size));
                        canvas.drawText(strings[0], x, y, TextAnchor.BASELINE_CENTER, getXAxisLabelPaint(), font);

                        y = (float) (labelArea.getHeight() - ((.5 * size) + 2));
                        canvas.drawText(strings[1], x, y, TextAnchor.BASELINE_CENTER, getXAxisLabelPaint(), font);
                    } else {
                        y = (float) (labelArea.getHeight() - (1.75 * size));
                        canvas.drawText(text, x, y, TextAnchor.BASELINE_CENTER, getXAxisLabelPaint(), font);
                    }
                }
            }

            x += barWidth + getBarMargin();
        }
    }

    protected void drawLabels(List<MultiColorBarData> dataPoints, List<ColorRect> rects, Canvas canvas) {
        if (Objects.equal(XAxisLabelPosition.NONE, getXAxisLabelPosition())) {
            return;
        }

        RectF labelArea = addHorizontalPadding(canvas.getBounds());
        drawLabels(dataPoints, canvas, labelArea);
    }

    protected void drawRects(Canvas canvas, List<ColorRect> rects) {
        for (ColorRect rect : rects) {
            if (rect.getGradientColorPair() == null) {
                setRectPaint(rect.getColor());
                canvas.drawFilledRectTopRounded(rect.getRectF(), getRectPaint(), cornerRadius);
            } else {
                canvas.drawGradientFilledRect(rect.getRectF(), rect.getGradientColorPair().getTopColor(), rect
                        .getGradientColorPair().getBottomColor());
            }
        }
    }

    protected void drawYAxisGridlines(Canvas canvas) {
        List<Double> yLines = ChartUtils.getGuidelines((int) getMinValue(), (int) getMaxValue(),
                getYAxisLabelsMaxCount());

        drawYAxisGridlines(canvas, getBounds(canvas), yLines);
    }

    protected void drawYAxisGridlines(Canvas canvas, RectF chartArea, List<Double> yLines) {
        float sideMargin = getYAxisLabelsSideMargin() + getLeftPadding();
        float left = 0, right = 0;

        if (Objects.equal(YAxisLabelPosition.NONE, getYAxisLabelPosition())) {
            left = canvas.getBounds().x;
            right = canvas.getBounds().x + canvas.getBounds().width;
        } else if (Objects.equal(YAxisLabelPosition.LEFT_OUTSIDE_CHARTAREA, getYAxisLabelPosition())
                || Objects.equal(YAxisLabelPosition.RIGHT_OUTSIDE_CHARTAREA, getYAxisLabelPosition())) {
            left = canvas.getBounds().x + sideMargin;
            right = canvas.getBounds().x + canvas.getBounds().width - sideMargin + 1; // UIUtils.GetPixelsFromDP(1);
        }

        Font font = getTheme().getRegularFont().deriveFont(getYAxisLabelTextSize());

        for (Double line : yLines) {
            float y = pointY(line, chartArea);
            canvas.drawLine(left, y, right, y, Theme.Strokes.SIMPLE_STROKE, getGridlinePaint());

            int precision = CurrencyFormat.ROUND;

            if (getYAxisLabelPosition() == YAxisLabelPosition.LEFT_OUTSIDE_CHARTAREA) {

                String text = I18NUtils.formatCurrency(line.intValue(), getCurrency(), getLocale(), CurrencyFormat.SHORT
                        | precision);

                float textX = getLeftPadding();
                float textY = (y + getYAxisLabelTextSize() / 4);

                canvas.drawText(text, textX, textY, TextAnchor.BASELINE_RIGHT, getYAxisLabelPaint(), font);
            }
        }
    }

    public float getAdditionalTopPadding() {
        return additionalTopPadding;
    }

    public float getAmountLabelTextSize() {
        return amountLabelTextSize;
    }

    public List<MultiColorBarData> getBarData() {
        return barData;
    }

    public float getBarMargin() {
        return barMargin;
    }

    protected int getBarMaxCount() {
        return Integer.MAX_VALUE;
    }

    public int getBarMinHeight() {
        return barMinHeight;
    }

    protected float getBarWidth(int barCount, RectF rectF) {
        float marginsWidth = (barCount - 1) * getBarMargin();
        return (rectF.width - marginsWidth) / barCount;
    }

    @Override
    protected float getBottomPadding() {
        float padding = super.getBottomPadding();

        padding += getAdditionalTopPadding();

        if (getMakeRoomForXLabels()) {
            padding += Math.round(1.75 * getXAxisLabelTextSize() + (getXAxisLabelBottomMargin()));
        }

        return padding;
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public Paint getGridlinePaint() {
        return gridlinePaint;
    }

    protected boolean getMakeRoomForXLabels() {
        return Objects.equal(XAxisLabelPosition.BOTTOM_OUTSIDE_CHARTAREA, getXAxisLabelPosition());
    }

    public double getMaxValue() {
        return maxValue;
    }

    protected float getMaxValue(List<MultiColorBarData> dataPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            return 0;
        }

        float max = Float.MIN_VALUE;

        for (MultiColorBarData barData : dataPoints) {
            float sum = 0;
            for (Float barValue : barData.getBarValues()) {
                sum += barValue;
            }

            if (sum > max) {
                max = sum;
            }
        }

        return max;
    }

    public double getMinValue() {
        return minValue;
    }

    public Paint getRectPaint() {
        return rectPaint;
    }

    protected List<ColorRect> getRects(List<MultiColorBarData> barData, RectF chartArea) {
        ArrayList<ColorRect> rects = Lists.newArrayList();

        int barCount = Math.min(barData.size(), getBarMaxCount());

        float maxHeight = chartArea.height;
        float barWidth = getBarWidth(barCount, chartArea);

        float currentLeft = chartArea.x + leftMargin;

        for (MultiColorBarData dataPoint : barData) {
            float currentBottom = chartArea.height + chartArea.y;

            for (int i = 0; i < getValidRectanglesCount(dataPoint); i++) {
                float currentValue = dataPoint.getBarValues().get(i);
                float rectHeight = Math.max(barMinHeight, maxHeight * currentValue / (float) getMaxValue());
                float top = currentBottom - rectHeight;

                RectF rect = new RectF(new Rectangle(Math.round(currentLeft), Math.round(top), Math.round(barWidth),
                        Math.round(currentBottom - top)));
                ColorRect colorRect = new ColorRect();
                colorRect.setRectF(rect);

                if (dataPoint.getGradientColorPairs() != null) {
                    colorRect.setGradientColorPair(dataPoint.getGradientColorPairs().get(i));
                } else {
                    colorRect.setColor(dataPoint.getBarColors().get(i));
                }

                if (i == getValidRectanglesCount(dataPoint) - 1) {
                    colorRect.setLabel(dataPoint.getAmountLabel());
                    colorRect.setLabelColor(dataPoint.getAmountLabelColor());
                }

                rects.add(colorRect);

                currentBottom = top;
            }

            currentLeft += barMargin;
            currentLeft += barWidth;
        }

        return rects;
    }

    protected int getValidRectanglesCount(MultiColorBarData dataPoint) {
        if (dataPoint.getGradientColorPairs() != null) {
            return Math.min(dataPoint.getBarValues().size(), dataPoint.getGradientColorPairs().size());
        } else {
            return Math.min(dataPoint.getBarValues().size(), dataPoint.getBarColors().size());
        }
    }

    protected float getXAxisLabelBottomMargin() {
        if (Objects.equal(XAxisLabelPosition.BOTTOM_OUTSIDE_CHARTAREA, getXAxisLabelPosition())) {
            return 14;
        } else {
            return 0;
        }
    }

    @Override
    public XAxisLabelPosition getXAxisLabelPosition() {
        return XAxisLabelPosition.BOTTOM_OUTSIDE_CHARTAREA;
    }

    protected float pointY(double value, RectF bounds) {
        return (float) (bounds.y + bounds.getHeight() * (float) (1 - (value - minValue) / (getMaxValue() - minValue)));
    }

    public void setAdditionalTopPadding(float padding) {
        this.additionalTopPadding = padding;
    }

    public void setAmountLabelTextSize(float textSize) {
        this.amountLabelTextSize = textSize;
    }

    public void setBarData(List<MultiColorBarData> data) {
        this.barData = data;
    }

    public void setBarMargin(float margin) {
        this.barMargin = margin;
    }

    public void setBarMinimumHeight(int height) {
        this.barMinHeight = height;
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
    }

    public void setGridlinePaint(Paint paint) {
        this.gridlinePaint = paint;
    }

    public void setLeftMargin(float margin) {
        this.leftMargin = margin;
    }

    public void setMaxValue(double value) {
        this.maxValue = value;
    }

    public void setMinValue(double value) {
        this.minValue = value;
    }

    public void setRectPaint(Paint paint) {
        this.rectPaint = paint;
    }

    protected class ColorRect {
        private Color color;
        private GradientColorPair gradientColorPair;
        private String label;
        private Color labelColor;
        private RectF rectF;

        public Color getColor() {
            return color;
        }

        public GradientColorPair getGradientColorPair() {
            return gradientColorPair;
        }

        public String getLabel() {
            return label;
        }

        public Color getLabelColor() {
            return labelColor;
        }

        public RectF getRectF() {
            return rectF;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void setGradientColorPair(GradientColorPair gradientColorPair) {
            this.gradientColorPair = gradientColorPair;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setLabelColor(Color labelColor) {
            this.labelColor = labelColor;
        }

        public void setRectF(RectF rectF) {
            this.rectF = rectF;
        }
    }

    public class GradientColorPair {
        private Color bottomColor;
        private Color topColor;

        public GradientColorPair() {
        }

        public GradientColorPair(Color topColor, Color bottomColor) {
            setTopColor(topColor);
            setBottomColor(bottomColor);
        }

        public Color getBottomColor() {
            return bottomColor;
        }

        public Color getTopColor() {
            return topColor;
        }

        public void setBottomColor(Color bottomColor) {
            this.bottomColor = bottomColor;
        }

        public void setTopColor(Color topColor) {
            this.topColor = topColor;
        }
    }

    public class MultiColorBarData {
        private String amountLabel;
        private Color amountLabelColor;
        private List<Color> barColors;
        private List<Float> barValues;
        private List<GradientColorPair> gradientColorPairs;
        private String label;
        private boolean labelBold;
        private Color labelColor;

        public String getAmountLabel() {
            return amountLabel;
        }

        public Color getAmountLabelColor() {
            return amountLabelColor;
        }

        public List<Color> getBarColors() {
            return barColors;
        }

        public List<Float> getBarValues() {
            return barValues;
        }

        public List<GradientColorPair> getGradientColorPairs() {
            return gradientColorPairs;
        }

        public String getLabel() {
            return label;
        }

        public Color getLabelColor() {
            return labelColor;
        }

        public boolean isLabelBold() {
            return labelBold;
        }

        public void setAmountLabel(String amountLabel) {
            this.amountLabel = amountLabel;
        }

        public void setAmountLabelColor(Color amountLabelColor) {
            this.amountLabelColor = amountLabelColor;
        }

        public void setBarColors(List<Color> barColors) {
            this.barColors = barColors;
        }

        public void setBarValues(List<Float> barValues) {
            this.barValues = barValues;
        }

        public void setGradientColorPairs(List<GradientColorPair> gradientColorPairs) {
            this.gradientColorPairs = gradientColorPairs;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setLabelBold(boolean labelBold) {
            this.labelBold = labelBold;
        }

        public void setLabelColor(Color labelColor) {
            this.labelColor = labelColor;
        }
    }
}
