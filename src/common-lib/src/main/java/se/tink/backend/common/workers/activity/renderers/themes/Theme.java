package se.tink.backend.common.workers.activity.renderers.themes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.joda.time.DateTime;
import se.tink.backend.common.Versions;
import se.tink.backend.core.TinkUserAgent;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.i18n.Catalog;

public abstract class Theme {

    protected LogUtils log;

    protected TinkUserAgent userAgent;

    protected boolean v2; // V2 is the option that is set when the theme is to be rendered on the new apps - FIXME: to be phased out eventually

    protected Theme(Class<? extends Theme> cls, Boolean version2) {
        v2 = version2;
        log = new LogUtils(cls);
    }

    public abstract String formatDate(Catalog catalog, Locale locale, DateTime date);

    public abstract BudgetSummaryTheme getBudgetSummaryTheme();

    public abstract LeftToSpendTheme getLeftToSpendTheme();
    
    public abstract List<SectionColor> getCategorizationProgressBackgroundSections();
    
    public abstract Color getCategorizationProgressColor(double progress);

    public abstract Font getRegularFont();

    public abstract Font getBoldFont();

    public abstract Font getLightFont();

    public Color getColor(ColorTypes type) {
        log.warn(String.format("Color not defined for type '%s'.", type.name()));
        return Colors.BLACK;
    }

    public Color getColor(ColorTypes type, int alpha) {
        return getColor(getColor(type), alpha);
    }

    public String getColorHex(ColorTypes type) {
        return toHtmlRgbColor(getColor(type));
    }

    public boolean isV2() {
        return v2;
    }

    public static Color getColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private static String toHtmlRgbColor(Color c) {
        String r = (c.getRed() < 16) ? "0" + Integer.toHexString(c.getRed()) : Integer.toHexString(c.getRed());
        String g = (c.getGreen() < 16) ? "0" + Integer.toHexString(c.getGreen()) : Integer.toHexString(c.getGreen());
        String b = (c.getBlue() < 16) ? "0" + Integer.toHexString(c.getBlue()) : Integer.toHexString(c.getBlue());
        return "#" + r + g + b;
    }

    public static Theme getTheme(Cluster cluster, TinkUserAgent userAgent) {
        if (Objects.equals(cluster, Cluster.ABNAMRO)) {
            return new AbnAmroTheme(Versions.shouldUseNewFeed(userAgent, cluster));
         } else {
            return new TinkTheme(Versions.shouldUseNewFeed(userAgent, cluster));
         }
    }

    public static final class Alpha {
        public static final int P0 = 0;
        public static final int P5 = 5 * 0xFF / 100;
        public static final int P8 = 8 * 0xFF / 100;
        public static final int P20 = 20 * 0xFF / 100;
        public static final int P30 = 30 * 0xFF / 100;
        public static final int P40 = 40 * 0xFF / 100;
        public static final int P50 = 50 * 0xFF / 100;
        public static final int P80 = 80 * 0xFF / 100;
    }

    public static final class Colors {
        public static final Color BLACK = new Color(0x0, 0x0, 0x0);
        public static final Color TRANSPARENT = new Color(0x0, 0x0, 0x0, 0x0);
        public static final Color WHITE = new Color(0xFF, 0xFF, 0xFF);
    }

    // TODO: Make the strokes theme specific
    public static final class Strokes {

        public static final Stroke DASH_STROKE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f,
                new float[] {
                        1, 2
                }, 0f);
        public static final Stroke SIMPLE_STROKE = new BasicStroke(1);

        public static final Stroke ROUNDED_STROKE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public abstract int getYAxisCurrencyFormat();
}
