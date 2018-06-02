package se.tink.backend.common.workers.activity.renderers;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.generators.models.FollowActivityData;
import se.tink.backend.common.workers.activity.renderers.models.ActivityHeader;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.svg.charts.BalanceLineChartArea;
import se.tink.backend.common.workers.activity.renderers.svg.charts.ChartArea.XAxisLabelPosition;
import se.tink.backend.common.workers.activity.renderers.svg.charts.ChartArea.YAxisLabelPosition;
import se.tink.backend.common.workers.activity.renderers.svg.charts.FeedIconPieChart;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.common.workers.activity.renderers.themes.Theme.Colors;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Balance;
import se.tink.backend.core.Category;
import se.tink.backend.core.Currency;
import se.tink.libraries.date.Period;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowData;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.utils.ChartUtils;
import se.tink.libraries.date.DateUtils;

public class FollowActivityRenderer extends BaseActivityRenderer {

    private static final int PIE_RADIUS = 32;
    private static final int ICON_BACKGROUND_INNER_RADIUS = (int) (PIE_RADIUS * 0.92f);
    private static final int ICON_SIZE = (int) (PIE_RADIUS * 1.25f);

    private static final byte LINE_CHART_ALPHA = 15 * 0xFF / 100;
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public FollowActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {

        FollowActivityData followActivityData = activity.getContent(FollowActivityData.class);
        FollowItem followItem = followActivityData.getFollowItem();
        ExpensesFollowCriteria criteria = SerializationUtils.deserializeFromString(followItem.getCriteria(),
                ExpensesFollowCriteria.class);
        FollowData data = followItem.getData();
        data.getPeriodAmounts();

        Catalog catalog = context.getCatalog();
        Currency currency = context.getUserCurrency();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("activity", activity);

        ActivityHeader headerData = new ActivityHeader();
        String icon = getIconSVG(activity);

        headerData.setLeftHeader(activity.getTitle());

        if (Objects.equal(followItem.getType(), FollowTypes.EXPENSES)) {
            Category c = context.getCategory(criteria.getCategoryIds().get(0));
            headerData.setLeftSubtext(c.getDisplayName());
        } else {
            headerData.setLeftSubtext(followItem.getName());
        }

        Double targetAmount = Math.abs(criteria.getTargetAmount());
        double currentAmount = Math.abs(data.getCurrentAmount());

        if (targetAmount < currentAmount) {
            double overdraftAmount = currentAmount - targetAmount;

            headerData.setRightHeader(I18NUtils.formatCurrency(currentAmount, currency, context.getLocale()));

            headerData.setRightSubtext(Catalog.format(catalog.getString("{0} overspent"),
                    I18NUtils.formatCurrency(overdraftAmount, currency, context.getLocale())));
        } else {
            double leftAmount = targetAmount - currentAmount;

            headerData.setRightHeader(Catalog.format(catalog.getString("{0} left"),
                    I18NUtils.formatCurrency(leftAmount, currency, context.getLocale())));

            headerData.setRightSubtext(Catalog.format(catalog.getString("of {0}"),
                    I18NUtils.formatCurrency(targetAmount, currency, context.getLocale())));
        }

        headerData.setDeepLink(getDeepLink(activity, followItem));

        params.put("svgIcon", icon);
        params.put("headerData", headerData);
        params.put("message", activity.getMessage());
        params.put("svg", getFollowLineGraph(activity, followItem));

        return render(Template.ACTIVITIES_BUDGET_LAYOUT_HTML, params);
    }

    private String getDeepLink(Activity activity, FollowItem followItem) {
        return deepLinkBuilderFactory.follow(followItem.getId())
                .withSource(getTrackingLabel(activity))
                .build();
    }

    protected String getIconSVG(Activity activity) {
        FollowActivityData followActivityData = activity.getContent(FollowActivityData.class);
        FollowItem item = followActivityData.getFollowItem();
        ExpensesFollowCriteria criteria = SerializationUtils.deserializeFromString(item.getCriteria(),
                ExpensesFollowCriteria.class);
        List<StringDoublePair> periodAmounts = item.getData().getPeriodAmounts();

        Canvas canvas = new Canvas(PIE_RADIUS * 2, PIE_RADIUS * 2);
        FeedIconPieChart iconChart = new FeedIconPieChart();

        double filledPart = periodAmounts.get(periodAmounts.size() - 1).getValue() / criteria.getTargetAmount();

        char icon;
        int iconSize = 0;
        Color backgroundColor;
        Color currentAmountColor;
        Color exceededAmountColor = context.getTheme().getColor(ColorTypes.WARNING);

        if (Objects.equal(item.getType(), FollowTypes.SEARCH)) {
            if (v2) {
                iconSize = (int) (ICON_SIZE * 0.9);
                icon = TinkIconUtils.IconsV2.SEARCH;
            } else {
                iconSize = (int) (ICON_SIZE * 0.72f); // 0.72 = 0.9 / 1.25, which is the ratio used in the summaries
                icon = TinkIconUtils.Icons.SEARCH;
            }
            backgroundColor = context.getTheme().getColor(ColorTypes.SEARCH);
            currentAmountColor = context.getTheme().getColor(ColorTypes.SEARCH_PIE_CHART);
        } else {
            if (v2) {
                icon = TinkIconUtils.getV2CategoryIcon(context.getCategory(criteria.getCategoryIds().get(0)));
                iconSize = (int) (ICON_SIZE * 0.9);
            } else {
                icon = TinkIconUtils.getV1CategoryIcon(context.getCategory(criteria.getCategoryIds().get(0)));
                iconSize = ICON_SIZE;
            }
            backgroundColor = context.getTheme().getColor(ColorTypes.EXPENSES);
            currentAmountColor = context.getTheme().getColor(ColorTypes.EXPENSES_PIE_CHART);
        }

        setupFeedIconPieChart(iconChart, backgroundColor, currentAmountColor, exceededAmountColor, icon, PIE_RADIUS,
                ICON_BACKGROUND_INNER_RADIUS, iconSize, filledPart);

        iconChart.draw(canvas, context.getTheme().isV2());
        return canvas.draw();
    }

    private void setupFeedIconPieChart(FeedIconPieChart iconChart, Color backgroundColor, Color currentAmountColor,
            Color exceededAmountColor, char icon, float outerRadius, float innerRadius, int iconSize,
            double filledPart) {
        setupFeedIcon(iconChart, backgroundColor, icon, outerRadius, iconSize);
        iconChart.setInnerRadius(innerRadius);
        iconChart.setCurrentAmountColor(currentAmountColor);
        iconChart.setFilledPart(filledPart);
        iconChart.setExceededAmountColor(exceededAmountColor);
    }

    private String getFollowLineGraph(Activity activity, FollowItem followItem) {
        List<Balance> balances = currentPeriodAmountsAsBalance(followItem);
        Double target = Math.abs(followItem.getFollowCriteria().getTargetAmount());
        double thisPeriodMax = ChartUtils.BalanceCharts.getMax(balances);

        List<DateTime> dates = getDatesInPeriod(context.getCleanPeriods(), followItem.getData().getPeriod());

        boolean targetExceeded = target < thisPeriodMax;

        double max = Math.max(1, Math.max(thisPeriodMax, target));
        double min = 0;
        Color pathColor = Colors.TRANSPARENT;
        Color areaColor;

        if (targetExceeded) {
            areaColor = Theme.getTheme(context.getCluster(), context.getUserAgent()).getColor(ColorTypes.WARNING);
        } else {
            if (Objects.equal(followItem.getType(), FollowTypes.SEARCH)) {
                areaColor = Theme.getTheme(context.getCluster(), context.getUserAgent()).getColor(ColorTypes.SEARCH);
            } else {
                areaColor = Theme.getTheme(context.getCluster(), context.getUserAgent()).getColor(ColorTypes.EXPENSES);
            }
        }

        areaColor = new Color(areaColor.getRed(), areaColor.getGreen(), areaColor.getBlue(), LINE_CHART_ALPHA);

        BalanceLineChartArea chart = new BalanceLineChartArea(context.getTheme(), context.getCatalog(),
                context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context.getLocale()));
        chart.setFill(true);
        chart.setAreaGradientBottomColor(areaColor);
        chart.setAreaGradientTopColor(areaColor);
        chart.setPathColor(pathColor);
        chart.setXAxisLabelPosition(XAxisLabelPosition.NONE);
        chart.setYAxisLabelPosition(YAxisLabelPosition.NONE);

        Canvas c = new Canvas(getSvgWidth(), 150);

        if (balances != null) {
            chart.setDates(dates);
            chart.setBalances(balances);
            chart.setMaxValue(max);
            chart.setMinValue(min);
        }

        chart.draw(c, v2);
        return c.draw();
    }

    private List<Balance> currentPeriodAmountsAsBalance(FollowItem followItem) {
        List<Balance> list = Lists.newArrayList();
        if (followItem.getData().getPeriodAmounts() != null) {
            for (StringDoublePair currentPeriodAmount : followItem.getData().getPeriodAmounts()) {
                list.add(createBalance(currentPeriodAmount.getKey(), -currentPeriodAmount.getValue()));
            }
        }
        return list;
    }

    private Balance createBalance(String date, double amount) {
        return new Balance(DateUtils.convertDate(date), amount);
    }

    public List<DateTime> getDatesInPeriod(List<Period> periods, String period) {
        List<DateTime> list = Lists.newArrayList();
        if (periods == null) {
            return list;
        }

        Period validPeriod = null;
        for (Period p : periods) {
            if (p.getName().equals(period)) {
                validPeriod = p;
                break;
            }
        }

        if (validPeriod == null) {
            return list;
        }

        return DateUtils.createDailyDateTimeList(validPeriod);
    }
}
