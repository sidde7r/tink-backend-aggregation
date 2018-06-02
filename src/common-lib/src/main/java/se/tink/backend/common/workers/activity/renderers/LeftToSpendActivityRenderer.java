package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.generators.models.LeftToSpendActivityData;
import se.tink.backend.common.workers.activity.renderers.models.ActivityHeader;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.svg.charts.ChartArea;
import se.tink.backend.common.workers.activity.renderers.svg.charts.ChartArea.XAxisLabelPosition;
import se.tink.backend.common.workers.activity.renderers.svg.charts.LeftToSpendLineChartArea;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.LeftToSpendTheme;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Balance;
import se.tink.backend.core.LeftToSpendBalance;
import se.tink.backend.core.Statistic;
import se.tink.backend.utils.ChartUtils;
import se.tink.libraries.date.DateUtils;

public class LeftToSpendActivityRenderer extends BaseActivityRenderer {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public LeftToSpendActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {

        final LeftToSpendActivityData data = activity.getContent(LeftToSpendActivityData.class);
        String svg = getSvgChart(activity);
        List<Statistic> leftToSpendStatistic = data.getLeftToSpend();

        Icon iconSvg;

        LeftToSpendTheme theme = context.getTheme().getLeftToSpendTheme();

        char icon;
        if (v2) {
            icon = TinkIconUtils.IconsV2.SALARY;
        } else {
            icon = TinkIconUtils.Icons.LEFT_TO_SPEND;
        }

        if (data.getDifference() > 0) {
            iconSvg = getIconSVG(theme.getPositiveIconType(), icon);
        } else {
            iconSvg = getIconSVG(theme.getNegativeIconType(), icon);
        }

        ActivityHeader headerData = new ActivityHeader();
        headerData.setIcon(iconSvg);
        headerData.setRightSubtext(I18NUtils.humanDateFormat(context.getCatalog(), context.getLocale(),
                activity.getDate()));
        headerData.setLeftHeader(activity.getTitle());
        headerData.setLeftSubtext(getAverageText(activity));
        headerData.setRightHeader(I18NUtils.formatCurrency(
                Math.round(leftToSpendStatistic.get(leftToSpendStatistic.size() - 1).getValue()),
                context.getUserCurrency(), context.getLocale()));
        headerData.setDeepLink(getDeepLink(activity));

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("headerData", headerData);
        params.put("innerTemplate", "inner-line-chart");
        params.put("svg", svg);

        return render(Template.ACTIVITIES_BASE_LAYOUT_HTML, params);
    }

    private String getAverageText(Activity activity) {
        final LeftToSpendActivityData data = activity.getContent(LeftToSpendActivityData.class);

        List<Statistic> leftToSpendStatistic = data.getLeftToSpend();
        List<Statistic> averageLeftToSpendStatistic = data.getLeftToSpendAverage();

        int lastIndex = leftToSpendStatistic.size() - 1;
        double diff = leftToSpendStatistic.get(lastIndex).getValue()
                - averageLeftToSpendStatistic.get(lastIndex).getValue();
        String formattedAmount = I18NUtils
                .formatCurrencyRound(Math.abs(diff), context.getUserCurrency(), context.getLocale());

        if (diff > 0) {
            return Catalog.format(context.getCatalog().getString("{0} above avg."), formattedAmount);
        } else {
            return Catalog.format(context.getCatalog().getString("{0} below avg."), formattedAmount);
        }
    }

    private String getDeepLink(Activity activity) {
        List<Statistic> statistics = activity.getContent(LeftToSpendActivityData.class).getLeftToSpend();

        return deepLinkBuilderFactory.leftToSpend()
                .withPeriod(statistics.get(statistics.size() - 1).getPeriod())
                .withSource(getTrackingLabel(activity))
                .build();
    }

    private String getSvgChart(Activity activity) {
        final LeftToSpendActivityData data = activity.getContent(LeftToSpendActivityData.class);

        List<Statistic> leftToSpendStatistic = data.getLeftToSpend();
        List<Statistic> averageLeftToSpendStatistic = data.getLeftToSpendAverage();
        Canvas canvas = new Canvas(getSvgWidth(), 150);

        List<LeftToSpendBalance> balances = ChartUtils.LeftToSpendCharts
                .getBalancesFromStatistics(leftToSpendStatistic);
        List<LeftToSpendBalance> meanBalances = ChartUtils.LeftToSpendCharts
                .getBalancesFromStatistics(averageLeftToSpendStatistic);

        double maxValue = ChartUtils.LeftToSpendCharts.getMax(balances, meanBalances);
        double minValue = ChartUtils.LeftToSpendCharts.getMin(balances, meanBalances);

        maxValue = Math.max(maxValue, 0);
        minValue = Math.min(minValue, 0);

        List<DateTime> dates = meanBalances.stream().map(Balance::getDate).collect(Collectors.toList());

        LeftToSpendLineChartArea averageChart = getAverageChart(balances);

        LeftToSpendLineChartArea currentChart = getCurrentChart();

        update(currentChart, balances, minValue, maxValue, dates);
        update(averageChart, meanBalances, minValue, maxValue, dates);

        currentChart.draw(canvas, v2);
        averageChart.draw(canvas, v2);

        return canvas.draw();
    }

    private LeftToSpendLineChartArea getAverageChart(List<LeftToSpendBalance> balances) {
        LeftToSpendLineChartArea averageChart = new LeftToSpendLineChartArea(context.getTheme(), context.getCatalog(),
                context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context.getLocale()));

        LeftToSpendTheme leftToSpendTheme = context.getTheme().getLeftToSpendTheme();

        averageChart.setXAxisLabelPosition(XAxisLabelPosition.BOTTOM_OUTSIDE_CHARTAREA);
        averageChart.setXAxisLabelPaint(context.getTheme().getColor(ColorTypes.CHART_AXIS_X_LABEL));
        averageChart.setShowXAxis(false);
        averageChart.setSmooth(true);
        averageChart.setMakeRoomForMarkToday(true);
        averageChart.setPathStroke(Theme.Strokes.DASH_STROKE);
        averageChart.setPathColor(leftToSpendTheme.getAverageChartPositivePathColor());
        averageChart.setNegativePathColor(leftToSpendTheme.getAverageChartNegativePathColor());
        averageChart.setYAxisLabelPosition(ChartArea.YAxisLabelPosition.NONE);
        averageChart.setXAxisLabelPaint(context.getTheme().getColor(ColorTypes.CHART_AXIS_X_LABEL));

        if (ChartUtils.BalanceCharts.getMin(new ArrayList<>(balances)) > 0) {
            averageChart.setXAxisPaint(leftToSpendTheme.getAverageChartXAxisPositiveColor());
            averageChart.setXAxisStroke(leftToSpendTheme.getAverageChartXAxisPositiveStroke());
        } else {
            averageChart.setXAxisPaint(leftToSpendTheme.getAverageChartXAxisNegativeColor());
            averageChart.setXAxisStroke(leftToSpendTheme.getAverageChartXAxisNegativeStroke());
        }

        averageChart.setShowYAxisGridlines(false);
        averageChart.setSmooth(leftToSpendTheme.isSmooth());

        return averageChart;
    }

    private LeftToSpendLineChartArea getCurrentChart() {
        LeftToSpendLineChartArea currentChart = new LeftToSpendLineChartArea(context.getTheme(), context.getCatalog(),
                context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context.getLocale()));

        LeftToSpendTheme leftToSpendTheme = context.getTheme().getLeftToSpendTheme();

        currentChart.setPathColor(leftToSpendTheme.getChartPositivePathColor());
        currentChart.setNegativePathColor(leftToSpendTheme.getChartNegativePathColor());
        currentChart.setFill(true);
        currentChart.setMarkToday(true);
        currentChart.setXAxisLabelPosition(XAxisLabelPosition.BOTTOM_OUTSIDE_CHARTAREA);
        currentChart.setXAxisLabelPaint(Theme.Colors.TRANSPARENT);
        currentChart.setYAxisLabelPaint(context.getTheme().getColor(ColorTypes.CHART_AXIS_Y_LABEL));
        currentChart.setTodayDotDiameter(leftToSpendTheme.getTodayDotDiameter());
        currentChart.setShowYAxisGridlines(leftToSpendTheme.showYAxisGridLines());
        currentChart.setShowXAxisGridlines(leftToSpendTheme.showXAxisGridLines());
        currentChart.setYAxisGridlineStroke(leftToSpendTheme.getYAxisGridLineStroke());
        currentChart.setPathStroke(leftToSpendTheme.getChartPathStroke());
        currentChart.setYAxisLabelIncludeZeroLine(leftToSpendTheme.showYAxisLabelZeroLine());

        currentChart.setAreaGradientTopColor(leftToSpendTheme.getChartPositiveAreaGradientTopColor());
        currentChart.setAreaGradientBottomColor(leftToSpendTheme.getChartPositiveAreaGradientBottomColor());
        currentChart.setNegativeAreaGradientBottomColor(leftToSpendTheme.getChartNegativeAreaGradientBottomColor());
        currentChart.setNegativeAreaGradientTopColor(leftToSpendTheme.getChartNegativeAreaGradientTopColor());

        currentChart.setHighlightXAxis(leftToSpendTheme.highlightXAxis());
        currentChart.setXAxisPaint(leftToSpendTheme.getXAxisColor());
        currentChart.setXAxisStroke(leftToSpendTheme.getXAxisStroke());
        currentChart.setSmooth(leftToSpendTheme.isSmooth());

        currentChart.setDrawXAxisOnTop(true);
        currentChart.setShowXAxis(true);

        return currentChart;
    }

    protected void update(LeftToSpendLineChartArea chartArea, List<LeftToSpendBalance> balances, double minValue,
            double maxValue, List<DateTime> dates) {
        chartArea.setDates(dates);
        chartArea.setMaxValue(maxValue);
        chartArea.setMinValue(minValue);
        chartArea.setBalances(new ArrayList<Balance>(balances));
    }
}
