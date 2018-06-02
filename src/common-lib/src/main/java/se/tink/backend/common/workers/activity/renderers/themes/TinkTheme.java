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

public class TinkTheme extends Theme {

    public TinkTheme(boolean v2) {
        super(TinkTheme.class, v2);
    }
    
    @Override
    public BudgetSummaryTheme getBudgetSummaryTheme() {
        return new BudgetSummaryThemeImpl();
    }

    @Override
    public LeftToSpendTheme getLeftToSpendTheme() {
        return new LeftToSpendThemeImpl();
    }

    private static final double LOW_CATEGORIZATION_LIMIT = 0.7d;
    private static final double OK_CATEGORIZATION_LIMIT = 0.9d;
    
    private static final List<SectionColor> CATEGORIZATION_SECTION_BACKGROUND_V1 = ImmutableList.of(
            new SectionColor(0, LOW_CATEGORIZATION_LIMIT, getColor(Colors.RED, 102)),
            new SectionColor(LOW_CATEGORIZATION_LIMIT, OK_CATEGORIZATION_LIMIT, getColor(Colors.ORANGE, 102)),
            new SectionColor(OK_CATEGORIZATION_LIMIT, 1, getColor(Colors.TURQUOISE, 102)));

    private static final List<SectionColor> CATEGORIZATION_SECTION_BACKGROUND_V2 = ImmutableList.of(
            new SectionColor(0, LOW_CATEGORIZATION_LIMIT, getColor(Colors.RED, 102)),
            new SectionColor(LOW_CATEGORIZATION_LIMIT, OK_CATEGORIZATION_LIMIT, getColor(Colors.ORANGE, 102)),
            new SectionColor(OK_CATEGORIZATION_LIMIT, 1, getColor(ColorsV2.TINK_ORANGE_LIGHT, 102)));

    @Override
    public List<SectionColor> getCategorizationProgressBackgroundSections() {
        if (v2) {
            return CATEGORIZATION_SECTION_BACKGROUND_V2;
        } else {
            return CATEGORIZATION_SECTION_BACKGROUND_V1;
        }
    }
    @Override
    public Color getCategorizationProgressColor(double progress) {

        if (progress < LOW_CATEGORIZATION_LIMIT) {
            return getColor(ColorTypes.CRITICAL);
        } else if (progress < OK_CATEGORIZATION_LIMIT) {
            return getColor(ColorTypes.WARNING);
        } else {
            if (v2) {
                return getColor(ColorTypes.PATH_COLOR);
            } else {
                return getColor(ColorTypes.POSITIVE);
            }
        }
    }

    @Override
    public Font getRegularFont() {
        if (v2) {
            return FontUtils.Fonts.LOTA_SEMIBOLD;
        } else {
            return FontUtils.Fonts.PROXIMA_NOVA_REGULAR;
        }
    }

    @Override
    public Font getBoldFont() {
        if (v2) {
            return FontUtils.Fonts.LOTA_BOLD;
        } else {
            return FontUtils.Fonts.PROXIMA_NOVA_SEMIBOLD;
        }
    }

    @Override
    public Font getLightFont() {
        if (v2) {
            return FontUtils.Fonts.LOTA_REGULAR;
        } else {
            return FontUtils.Fonts.PROXIMA_NOVA_LIGHT;
        }
    }

    @Override
    public Color getColor(ColorTypes type) {

        if (v2) {
            switch (type) {
            case BANK_FEE:
                return ColorsV2.AMARANTH_PINK;
            case COMPARISON:
                return ColorsV2.GREY_LIGHTER;
            case CRITICAL:
                return ColorsV2.RED;
            case CHART_AXIS:
                return ColorsV2.GREY_LIGHTEST;
            case CHART_AXIS_X_LABEL:
            case CHART_AXIS_Y_LABEL:
                return ColorsV2.GREY_LIGHT;
            case DEFAULT:
                return ColorsV2.TINK_ORANGE;
            case EXPENSES:
                return ColorsV2.BLUE;
            case EXPENSES_COMPARISON:
                return ColorsV2.BLUE_P20;
            case EXPENSES_PIE_CHART:
                return ColorsV2.BLUE_P50;
            case INCOME:
                return ColorsV2.GREEN;
            case INFO:
                return ColorsV2.PINK;
            case POSITIVE:
                return ColorsV2.TURQUOISE;
            case SEARCH:
                return ColorsV2.BLUE;
            case SEARCH_PIE_CHART:
                return ColorsV2.BLUE_P50;
            case TRANSFERS:
                return ColorsV2.GREY;
            case WARNING:
                return ColorsV2.ORANGE;
            case CHART_AXIS_X_LABEL_HIGHLIGHTED:
                return ColorsV2.BLACK;
            case PATH_COLOR:
                return ColorsV2.TINK_ORANGE;
            default:
                return super.getColor(type);
            }
        } else {
            switch (type) {
            case BANK_FEE:
                return Colors.AMARANTH_PINK;
            case COMPARISON:
                return Colors.GREY_LIGHTER;
            case CRITICAL:
                return Colors.RED;
            case CHART_AXIS:
                return Colors.GREY_LIGHTEST;
            case CHART_AXIS_X_LABEL:
            case CHART_AXIS_Y_LABEL:
                return Colors.GREY_LIGHT;
            case DEFAULT:
                return Colors.TURQUOISE;
            case EXPENSES:
                return Colors.BLUE;
            case EXPENSES_COMPARISON:
                return Colors.BLUE_P20;
            case EXPENSES_PIE_CHART:
                return Colors.BLUE_P50;
            case INCOME:
                return Colors.GREEN;
            case INFO:
                return Colors.PINK;
            case POSITIVE:
                return Colors.TURQUOISE;
            case SEARCH:
                return Colors.PINK;
            case SEARCH_PIE_CHART:
                return Colors.PINK_P50;
            case TRANSFERS:
                return Colors.GREY;
            case WARNING:
                return Colors.ORANGE;
            case CHART_AXIS_X_LABEL_HIGHLIGHTED:
                return Colors.BLACK;
            case PATH_COLOR:
                return Colors.TURQUOISE;
            default:
                return super.getColor(type);
            }
        }
    }

    @Override
    public String formatDate(Catalog catalog, Locale locale, DateTime date) {
        return I18NUtils.formatVeryShortDate(catalog, locale, date);
    }

    @Override
    public int getYAxisCurrencyFormat() {
        return I18NUtils.CurrencyFormat.NONE | I18NUtils.CurrencyFormat.SHORT;
    }

    private static final class Colors {
        private static final Color AMARANTH_PINK = new Color(0xF2, 0xB9, 0xBC);
        private static final Color BLUE = new Color(0x00, 0x9D, 0xDE);
        private static final Color BLUE_P20 = getColor(BLUE, Alpha.P20);
        private static final Color BLUE_P50 = getColor(BLUE, Alpha.P50);
        private static final Color GREEN = new Color(0x61, 0xBC, 0x45);
        private static final Color GREY = new Color(0x80, 0x80, 0x80);
        private static final Color GREY_LIGHT = new Color(0xBB, 0xBB, 0xBB);
        private static final Color GREY_LIGHTER = new Color(0xCC, 0xCC, 0xCC);
        private static final Color GREY_LIGHTEST = new Color(0xE5, 0xE5, 0xE5);
        private static final Color ORANGE = new Color(0xFC, 0xB8, 0x27);
        private static final Color PINK = new Color(0xC7, 0x63, 0xBF);
        private static final Color PINK_P50 = getColor(PINK, Alpha.P50);
        private static final Color RED = new Color(0xE2, 0x37, 0x3F);
        private static final Color TURQUOISE = new Color(0x00, 0xBD, 0xBD);
        private static final Color BLACK = new Color(0x00, 0x00, 0x00);
    }

    private static final class ColorsV2 {
        private static final Color AMARANTH_PINK = new Color(0xF2, 0xB9, 0xBC);
        private static final Color BLUE = new Color(14, 158, 194);
        private static final Color BLUE_P20 = getColor(BLUE, Alpha.P20);
        private static final Color BLUE_P50 = getColor(BLUE, Alpha.P50);
        private static final Color GREEN = new Color(54, 180, 114);
        private static final Color GREY = new Color(0x80, 0x80, 0x80);
        private static final Color GREY_LIGHT = new Color(0xBB, 0xBB, 0xBB);
        private static final Color GREY_LIGHTER = new Color(0xCC, 0xCC, 0xCC);
        private static final Color GREY_LIGHTEST = new Color(0xE5, 0xE5, 0xE5);
        private static final Color ORANGE = new Color(252, 184, 39);
        private static final Color PINK = new Color(0xC7, 0x63, 0xBF);
        private static final Color PINK_P50 = getColor(PINK, Alpha.P50);
        private static final Color RED = new Color(230, 63, 72);
        private static final Color TURQUOISE = new Color(0x00, 0xBD, 0xBD);
        private static final Color BLACK = new Color(0x00, 0x00, 0x00);
        private static final Color TINK_ORANGE = new Color(248, 149, 114);
        private static final Color TINK_ORANGE_LIGHT = new Color(254, 234, 227);
    }

    class BudgetSummaryThemeImpl implements BudgetSummaryTheme {
        public int getMinPieMargin() {
            return 8;
        }

        public int getMaxPieMargin() {
            return 32;
        }

        public int getMaxPieRadius() {
            return 32;
        }
    }

    class LeftToSpendThemeImpl implements LeftToSpendTheme {

        @Override
        public Stroke getChartPathStroke() {
            return new BasicStroke(2);
        }

        @Override
        public String getPositiveIconType() {
            if (v2) {
               return Icon.IconColorTypes.WARNING;
            } else {
                return Icon.IconColorTypes.POSITIVE;
            }
        }

        @Override
        public String getNegativeIconType() {
            return Icon.IconColorTypes.WARNING;
        }

        @Override
        public Stroke getAverageChartXAxisNegativeStroke() {
            return Strokes.SIMPLE_STROKE;
        }

        @Override
        public Stroke getAverageChartXAxisPositiveStroke() {
            return Strokes.DASH_STROKE;
        }

        @Override
        public Color getAverageChartXAxisNegativeColor() {
            if (v2) {
                return getColor(ColorTypes.DEFAULT);
            } else {
                return getColor(ColorTypes.WARNING);
            }
        }

        @Override
        public Color getAverageChartXAxisPositiveColor() {
            return getColor(ColorTypes.CHART_AXIS, Theme.Alpha.P50);
        }

        @Override
        public Color getAverageChartPositivePathColor() {
            return getColor(ColorTypes.COMPARISON);
        }

        @Override
        public Color getAverageChartNegativePathColor() {
            return getColor(ColorTypes.COMPARISON);
        }

        @Override
        public int getTodayDotDiameter() {
            return 4;
        }

        @Override
        public Color getChartPositivePathColor() {
            return getColor(ColorTypes.DEFAULT);
        }

        @Override
        public Color getChartNegativePathColor() {
            if (v2) {
                return getColor(ColorTypes.DEFAULT);
            } else {
                return getColor(ColorTypes.CRITICAL);
            }
        }

        @Override
        public Color getChartNegativeAreaGradientBottomColor() {
            if (v2) {
                return getColor(ColorTypes.DEFAULT, Theme.Alpha.P40);
            } else {
                return getColor(ColorTypes.CRITICAL, Theme.Alpha.P40);
            }
        }

        @Override
        public Color getChartNegativeAreaGradientTopColor() {
            if (v2) {
                return getColor(ColorTypes.DEFAULT, Theme.Alpha.P0);
            } else {
                return getColor(ColorTypes.CRITICAL, Theme.Alpha.P0);
            }
        }

        @Override
        public Color getChartPositiveAreaGradientBottomColor() {
            return getColor(ColorTypes.DEFAULT, Theme.Alpha.P0);
        }

        @Override
        public Color getChartPositiveAreaGradientTopColor() {
            return getColor(ColorTypes.DEFAULT, Theme.Alpha.P40);
        }

        @Override
        public boolean showYAxisGridLines() {
            return false;
        }

        @Override
        public boolean showXAxisGridLines() {
            return false;
        }

        @Override
        public Stroke getYAxisGridLineStroke() {
            return null;
        }

        @Override
        public boolean showYAxisLabelZeroLine() {
            return false;
        }

        @Override
        public boolean highlightXAxis() {
            return false;
        }

        @Override
        public Color getXAxisColor() {
            return null;
        }

        @Override
        public Stroke getXAxisStroke() {
            return new BasicStroke(1f);
        }

        @Override
        public boolean isSmooth() {
            return false;
        }
    }
}
