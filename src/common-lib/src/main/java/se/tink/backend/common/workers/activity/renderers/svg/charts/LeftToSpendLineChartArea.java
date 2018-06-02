package se.tink.backend.common.workers.activity.renderers.svg.charts;

import java.util.Calendar;
import java.util.Locale;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Currency;

public class LeftToSpendLineChartArea extends BalanceLineChartArea {

    public LeftToSpendLineChartArea(Theme theme, Catalog catalog, Currency currency, Locale locale, Calendar calendar) {
        super(theme, catalog, currency, locale, calendar);
    }

    @Override
    protected float getBottomPadding() {
        float padding = 0;

        if (getMinValue() < 0) {
            padding += 1; // Make at least room for the stroke
        }

        if (getMakeRoomForXLabels()) {
            padding += getXAxisLabelTextSize() + (getXLabelBottomMargin() * 2);
        }

        return padding;
    }
}
