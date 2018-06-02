package se.tink.backend.common.workers.activity.renderers.themes;

import com.google.common.collect.ImmutableList;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.List;
import java.util.Locale;
import org.joda.time.DateTime;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.utils.FontUtils;
import se.tink.libraries.i18n.Catalog;

public class AbnAmroTheme extends Theme {

    public AbnAmroTheme(boolean v2) {
        super(AbnAmroTheme.class, v2);
    }

    @Override
    public BudgetSummaryTheme getBudgetSummaryTheme() {
        return new BudgetSummaryThemeImpl();
    }

    @Override
    public LeftToSpendTheme getLeftToSpendTheme() {
        return new LeftToSpendThemeImpl();
    }

    @Override
    public List<SectionColor> getCategorizationProgressBackgroundSections() {
        return ImmutableList.of(
                new SectionColor(0, 1, getColor(ColorTypes.GREY_LIGHTEST)));
    }
    
    @Override
    public Color getCategorizationProgressColor(double progress) {
        return getColor(ColorTypes.INCOME);
    }

    @Override
    public Font getRegularFont() {
        if (v2) {
            return FontUtils.Fonts.BROWN_REGULAR;
        } else {
            return FontUtils.Fonts.PROXIMA_NOVA_REGULAR;
        }
        // TODO: change this when we're ready to test new fonts.
        //return userAgent.isIOS() ? FontUtils.Fonts.IOS_SYSTEM_REGULAR : FontUtils.Fonts.PROXIMA_NOVA_REGULAR;
    }

    @Override
    public Font getBoldFont() {
        if (v2) {
            return FontUtils.Fonts.BROWN_BOLD;
        } else {
            return FontUtils.Fonts.PROXIMA_NOVA_SEMIBOLD;
        }
        // TODO: change this when we're ready to test new fonts.
        //return userAgent.isIOS() ? FontUtils.Fonts.IOS_SYSTEM_SEMIBOLD : FontUtils.Fonts.PROXIMA_NOVA_SEMIBOLD;
    }

    @Override
    public Font getLightFont() {
        if (v2) {
            return FontUtils.Fonts.BROWN_LIGHT;
        } else {
            return FontUtils.Fonts.PROXIMA_NOVA_LIGHT;
        }
        // TODO: change this when we're ready to test new fonts.
        //return userAgent.isIOS() ? FontUtils.Fonts.IOS_SYSTEM_LIGHT : FontUtils.Fonts.PROXIMA_NOVA_LIGHT;
    }

    @Override
    public Color getColor(ColorTypes type) {
        Colors colors;

        if (v2) {
            colors = new ColorsV2();
        } else {
           colors = new ColorsV1();
        }

        switch (type) {
        case COMPARISON:
            return colors.getGrayLighter();
        case CRITICAL:
            return colors.getCritical();
        case CHART_AXIS:
            return colors.getGrayLightest();
        case CHART_AXIS_X_LABEL:
        case CHART_AXIS_Y_LABEL:
            return colors.getGrayLight();
        case DEFAULT:
            return colors.getDefault();
        case EXPENSES:
            return colors.getExpenses();
        case EXPENSES_COMPARISON:
            return colors.getExpensesComparision();
        case EXPENSES_PIE_CHART:
            return colors.getExpensesP50();
        case INCOME:
            return colors.getIncome();
        case INFO:
            return colors.getInfo();
        case POSITIVE:
            return colors.getPositive();
        case SEARCH:
            return colors.getExpenses();
        case SEARCH_PIE_CHART:
            return colors.getExpensesP50();
        case TRANSFERS:
            return colors.getGrey();
        case WARNING:
            return colors.getWarning();
        case CHART_AXIS_X_LABEL_HIGHLIGHTED:
            return colors.getDefault();
        case PATH_COLOR:
            return colors.getPathColor();
        case GREY:
            return colors.getGrey();
        case GREY_LIGHT:
            return colors.getGrayLight();
        case GREY_LIGHTEST:
            return colors.getGrayLightest();
        case LEFT_TO_SPEND:
            return colors.getLeftToSpend();
        default:
            return super.getColor(type);
        }
    }

    @Override
    public String formatDate(Catalog catalog, Locale locale, DateTime date) {
        return I18NUtils.formatShortDate(catalog, locale, date);
    }

    @Override
    public int getYAxisCurrencyFormat() {
        return I18NUtils.CurrencyFormat.SHORT | I18NUtils.CurrencyFormat.SYMBOL;
    }

    private interface Colors {
        Color getExpenses();
        Color getExpensesComparision();
        Color getExpensesP50();
        Color getIncome();
        Color getLeftToSpend();

        Color getGrey();
        Color getGrayLight();
        Color getGrayLighter();
        Color getGrayLightest();

        Color getPositive();
        Color getInfo();
        Color getWarning();
        Color getCritical();

        Color getDefault();
        Color getPathColor();
    }

    private static final class ColorsV1 implements Colors {

        private static final Color EXPENSES = new Color(0x00, 0x92, 0x86);
        private static final Color EXPENSES_COMPARISON = new Color(0x7A, 0xC6, 0xC0);
        private static final Color EXPENSES_P50 = getColor(EXPENSES, Alpha.P50);
        private static final Color INCOME = new Color(0x94, 0xC2, 0x3C);
        private static final Color LEFT_TO_SPEND = new Color(13, 147, 175);

        private static final Color GREY = new Color(0x80, 0x80, 0x80);
        private static final Color GREY_LIGHT = new Color(0xBB, 0xBB, 0xBB);
        private static final Color GREY_LIGHTER = new Color(0xCC, 0xCC, 0xCC);
        private static final Color GREY_LIGHTEST = new Color(0xE6, 0xE6, 0xE6);

        private static final Color POSITIVE = new Color(0x00, 0x92, 0x86);
        private static final Color INFO = new Color(0xF3, 0xC0, 0x00);
        private static final Color WARNING = new Color(0xFF, 0x66, 0x00);
        private static final Color CRITICAL = new Color(0xFF, 0x66, 0x00);
        
        private static final Color DEFAULT = new Color(0x77, 0x77, 0x77);

        private static final Color PATH_COLOR = DEFAULT;

        @Override public Color getExpenses() {
            return EXPENSES;
        }

        @Override public Color getExpensesComparision() {
            return EXPENSES_COMPARISON;
        }

        @Override public Color getExpensesP50() {
            return EXPENSES_P50;
        }

        @Override public Color getIncome() {
            return INCOME;
        }

        @Override public Color getLeftToSpend() {
            return LEFT_TO_SPEND;
        }

        @Override public Color getGrey() {
            return GREY;
        }

        @Override public Color getGrayLight() {
            return GREY_LIGHT;
        }

        @Override public Color getGrayLighter() {
            return GREY_LIGHTER;
        }

        @Override public Color getGrayLightest() {
            return GREY_LIGHTEST;
        }

        @Override public Color getPositive() {
            return POSITIVE;
        }

        @Override public Color getInfo() {
            return INFO;
        }

        @Override public Color getWarning() {
            return WARNING;
        }

        @Override public Color getCritical() {
            return CRITICAL;
        }

        @Override public Color getDefault() {
            return DEFAULT;
        }

        @Override public Color getPathColor() {
            return PATH_COLOR;
        }
    }

    private static final class ColorsV2 implements Colors {

        private static final Color EXPENSES = new Color(90, 80, 199);
        private static final Color EXPENSES_COMPARISON = new Color(231, 229, 247);
        private static final Color EXPENSES_P50 = getColor(EXPENSES, Alpha.P50);
        private static final Color INCOME = new Color(59, 210, 158);
        private static final Color LEFT_TO_SPEND = new Color(16, 168, 200);

        private static final Color GREY = new Color(0x80, 0x80, 0x80);
        private static final Color GREY_LIGHT = new Color(0xBB, 0xBB, 0xBB);
        private static final Color GREY_LIGHTER = new Color(0xCC, 0xCC, 0xCC);
        private static final Color GREY_LIGHTEST = new Color(0xE6, 0xE6, 0xE6);

        private static final Color POSITIVE = new Color(59, 210, 158);
        private static final Color INFO = new Color(90, 80, 199);
        private static final Color WARNING = new Color(245, 137, 35);
        private static final Color CRITICAL = new Color(245, 137, 35);

        private static final Color DEFAULT = new Color(0x77, 0x77, 0x77);

        private static final Color PATH_COLOR = LEFT_TO_SPEND;

        @Override public Color getExpenses() {
            return EXPENSES;
        }

        @Override public Color getExpensesComparision() {
            return EXPENSES_COMPARISON;
        }

        @Override public Color getExpensesP50() {
            return EXPENSES_P50;
        }

        @Override public Color getIncome() {
            return INCOME;
        }

        @Override public Color getLeftToSpend() {
            return LEFT_TO_SPEND;
        }

        @Override public Color getGrey() {
            return GREY;
        }

        @Override public Color getGrayLight() {
            return GREY_LIGHT;
        }

        @Override public Color getGrayLighter() {
            return GREY_LIGHTER;
        }

        @Override public Color getGrayLightest() {
            return GREY_LIGHTEST;
        }

        @Override public Color getPositive() {
            return POSITIVE;
        }

        @Override public Color getInfo() {
            return INFO;
        }

        @Override public Color getWarning() {
            return WARNING;
        }

        @Override public Color getCritical() {
            return CRITICAL;
        }

        @Override public Color getDefault() {
            return DEFAULT;
        }

        @Override public Color getPathColor() {
            return PATH_COLOR;
        }
    }
    
    class BudgetSummaryThemeImpl implements BudgetSummaryTheme {
        public int getMinPieMargin() {
            return 8;
        }

        public int getMaxPieMargin() {
            return 24;
        }

        public int getMaxPieRadius() {
            return 24;
        }
    }

    class LeftToSpendThemeImpl implements LeftToSpendTheme {

        @Override
        public Stroke getChartPathStroke() {
            return Strokes.ROUNDED_STROKE;
        }

        @Override
        public String getPositiveIconType() {
            return Icon.IconColorTypes.LEFT_TO_SPEND;
        }

        @Override
        public String getNegativeIconType() {
            return Icon.IconColorTypes.LEFT_TO_SPEND;
        }

        @Override
        public Stroke getAverageChartXAxisNegativeStroke() {
            return Strokes.ROUNDED_STROKE;
        }

        @Override
        public Stroke getAverageChartXAxisPositiveStroke() {
            return Strokes.ROUNDED_STROKE;
        }

        @Override
        public Color getAverageChartXAxisNegativeColor() {
            return getColor(ColorTypes.CHART_AXIS, Theme.Alpha.P50);
        }

        @Override
        public Color getAverageChartXAxisPositiveColor() {
            return getColor(ColorTypes.CHART_AXIS, Theme.Alpha.P50);
        }

        @Override
        public Color getAverageChartPositivePathColor() {
            return getColor(ColorTypes.LEFT_TO_SPEND);
        }

        @Override
        public Color getAverageChartNegativePathColor() {
            return getColor(ColorTypes.LEFT_TO_SPEND);
        }

        @Override
        public int getTodayDotDiameter() {
            return 3;
        }

        @Override
        public Color getChartPositivePathColor() {
            return getColor(ColorTypes.PATH_COLOR);
        }

        @Override
        public Color getChartNegativePathColor() {
            return getColor(ColorTypes.CRITICAL);
        }

        @Override
        public Color getChartNegativeAreaGradientBottomColor() {
            return getColor(ColorTypes.CRITICAL, Theme.Alpha.P40);
        }

        @Override
        public Color getChartNegativeAreaGradientTopColor() {
            return getColor(ColorTypes.CRITICAL, Theme.Alpha.P5);
        }

        @Override
        public Color getChartPositiveAreaGradientBottomColor() {
            return getColor(ColorTypes.PATH_COLOR, Theme.Alpha.P5);
        }

        @Override
        public Color getChartPositiveAreaGradientTopColor() {
            return getColor(ColorTypes.PATH_COLOR, Theme.Alpha.P40);
        }

        @Override
        public boolean showYAxisGridLines() {
            if (v2) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean showXAxisGridLines() {
            return false;
        }

        @Override
        public Stroke getYAxisGridLineStroke() {
            return new BasicStroke(0.5f);
        }

        @Override
        public boolean showYAxisLabelZeroLine() {
            return true;
        }

        @Override
        public boolean highlightXAxis() {
            return true;
        }

        @Override
        public Color getXAxisColor() {
            return getColor(ColorTypes.GREY_LIGHT);
        }

        @Override
        public Stroke getXAxisStroke() {
            return Strokes.SIMPLE_STROKE;
        }

        @Override
        public boolean isSmooth() {
            return true;
        }
    }
}
