package se.tink.backend.common.workers.activity.renderers.svg.charts;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.I18NUtils.CurrencyFormat;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Currency;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.TinkUserAgent;

public class KVPairBarChart extends MultiColorBarChartArea {

    private Color barColor;
    private float horizontalPadding;
    private boolean labelBold;
    private Color labelColor;
    private Color labelMarkedColor;
    private List<KVPair<String, Double>> labelMarkedValues = Lists.newLinkedList();
    private int labelMarkedValuesCurrencyFormat;
    private boolean leaveRoomForAmountLabels;
    private Color markedBarColor;
    private List<KVPair<String, Double>> markedValues = Lists.newLinkedList();
    private List<KVPair<String, Double>> values;

    public KVPairBarChart(Theme theme, Catalog catalog, Currency currency, Locale locale, Calendar calendar,
            TinkUserAgent userAgent) {
        super(theme, catalog, currency, locale, calendar, userAgent);
        
        setLabelMarkedValuesCurrencyFormat(CurrencyFormat.ROUND);
    }
    
    @Override
    public void draw(Canvas canvas) {
        updateBarData();
        super.draw(canvas);
    }
    
    private float getAmountLabelMargin() {
        return 6;
    }

    public Color getBarColor() {
        return barColor;
    }

    public String getFormattedAmountLabel(double amount) {
        return I18NUtils.formatCurrency(amount, getCurrency(), getLocale(), labelMarkedValuesCurrencyFormat);
    }

    @Override
    public float getHorizontalPadding() {
        return horizontalPadding;
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public Color getLabelMarkedColor() {
        return labelMarkedColor;
    }

    public List<KVPair<String, Double>> getLabelMarkedValues() {
        return labelMarkedValues;
    }

    public Color getMarkedBarColor() {
        return markedBarColor;
    }

    public List<KVPair<String, Double>> getMarkedValues() {
        return markedValues;
    }

    @Override
    protected float getTopPadding() {
        float padding = 0;
        
        if (isLeaveRoomForAmountLabels()) {
            padding += getAmountLabelTextSize() + (getAmountLabelMargin() * 2);
        }

        return padding;
    }

    public List<KVPair<String, Double>> getValues() {
        return values;
    }

    private boolean isLabelBold() {
        return labelBold;
    }

    private boolean isLeaveRoomForAmountLabels() {
        return leaveRoomForAmountLabels;
    }

    public void setBarColor(Color color) {
        this.barColor = color;
    }

    public void setHorizontalPadding(float padding) {
        this.horizontalPadding = padding;
    }

    public void setLabelBold(boolean bold) {
        this.labelBold = bold;
    }

    public void setLabelColor(Color color) {
        this.labelColor = color;
    }

    public void setLabelMarkedColor(Color color) {
        this.labelMarkedColor = color;
    }

    public void setLabelMarkedValues(List<KVPair<String, Double>> values) {
        this.labelMarkedValues = values;
    }

    public void setLabelMarkedValuesCurrencyFormat(int currencyFormat) {
        this.labelMarkedValuesCurrencyFormat = currencyFormat;
    }

    public void setLeaveRoomForAmountLabels(boolean leaveRoom) {
        this.leaveRoomForAmountLabels = leaveRoom;
    }

    public void setMarkedBarColor(Color color) {
        this.markedBarColor = color;
    }

    public void setMarkedValues(List<KVPair<String, Double>> values) {
        this.markedValues = values;
    }

    public void setValues(List<KVPair<String, Double>> values) {
        this.values = values;
    }

    private void updateBarData() {
        List<MultiColorBarData> data = Lists.newArrayList();
        
        for (KVPair<String, Double> value : getValues()) {
            ArrayList<Color> colorList = Lists.newArrayList();
            ArrayList<Float> valueList = Lists.newArrayList();
            String label = value.getKey();
            valueList.add((float) value.getValue().doubleValue());

            if (getMarkedValues().contains(value)) {
                colorList.add(getMarkedBarColor());
            } else {
                colorList.add(getBarColor());
            }

            MultiColorBarData barData = new MultiColorBarData();
            barData.setBarColors(colorList);
            barData.setBarValues(valueList);
            barData.setLabel(label);
            barData.setLabelColor(getLabelColor());
            barData.setLabelBold(isLabelBold());

            if (getLabelMarkedValues().contains(value)) {
                barData.setAmountLabel(getFormattedAmountLabel(value.getValue()));
                
                Color color = getLabelMarkedColor();
                if (color == null) {
                    color = getBarColor();
                }
                
                barData.setAmountLabelColor(color);
            }

            data.add(barData);
        }
        
        setBarData(data);
    }
}
