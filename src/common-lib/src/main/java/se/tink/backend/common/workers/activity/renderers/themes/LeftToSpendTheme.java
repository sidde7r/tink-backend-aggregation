package se.tink.backend.common.workers.activity.renderers.themes;

import java.awt.Color;
import java.awt.Stroke;

public interface LeftToSpendTheme {
    // Icon theme
    String getPositiveIconType();
    String getNegativeIconType();

    // Average chart details
    Stroke getAverageChartXAxisNegativeStroke();
    Stroke getAverageChartXAxisPositiveStroke();
    Color getAverageChartXAxisNegativeColor();
    Color getAverageChartXAxisPositiveColor();
    Color getAverageChartPositivePathColor();
    Color getAverageChartNegativePathColor();

    // Chart details
    Stroke getChartPathStroke();
    int getTodayDotDiameter();
    Color getChartPositivePathColor();
    Color getChartNegativePathColor();

    Color getChartNegativeAreaGradientBottomColor();
    Color getChartNegativeAreaGradientTopColor();
    Color getChartPositiveAreaGradientBottomColor();
    Color getChartPositiveAreaGradientTopColor();

    boolean showYAxisGridLines();
    boolean showXAxisGridLines();
    Stroke getYAxisGridLineStroke();
    boolean showYAxisLabelZeroLine();

    boolean highlightXAxis();
    Color getXAxisColor();
    Stroke getXAxisStroke();
    boolean isSmooth();
}
