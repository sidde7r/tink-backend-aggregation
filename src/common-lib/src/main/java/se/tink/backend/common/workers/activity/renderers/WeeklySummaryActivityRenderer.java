package se.tink.backend.common.workers.activity.renderers;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import se.tink.backend.common.Versions;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.generators.models.FollowActivityFeedbackData;
import se.tink.backend.common.workers.activity.generators.models.WeeklySummaryActivityData;
import se.tink.backend.common.workers.activity.renderers.models.BiggestPurchaseData;
import se.tink.backend.common.workers.activity.renderers.models.BudgetSummaryData;
import se.tink.backend.common.workers.activity.renderers.models.HtmlSvgSection;
import se.tink.backend.common.workers.activity.renderers.models.TotalExpensesData;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.svg.charts.FeedIconPieChart;
import se.tink.backend.common.workers.activity.renderers.svg.charts.KVPairBarChart;
import se.tink.backend.common.workers.activity.renderers.themes.BudgetSummaryTheme;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.common.workers.activity.renderers.utils.BudgetSummaryLayoutHelper;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class WeeklySummaryActivityRenderer extends BaseActivityRenderer {

    private static final int MAX_NR_BUDGET_PIES = 6;
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public WeeklySummaryActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);

        WeeklySummaryActivityData data = activity.getContent(WeeklySummaryActivityData.class);

        if (data.getBiggestExpense() == null || data.getBiggestExpense().getAmount() == 0) {
            return "";
        }

        Catalog catalog = context.getCatalog();

        String title = context.getUser().getProfile().getLocale().equals("sv_SE") ? Catalog.format(
                catalog.getString("Week {0}"), data.getWeek()) : catalog.getString("Weekly summary");
        params.put("period", title);

        String subtitle = "";

        if (data.getWeekEndDate() != null && data.getWeekStartDate() != null) {

            String start = I18NUtils.formatShortDate(catalog, context.getLocale(), data.getWeekStartDate());
            String end = I18NUtils.formatShortDate(catalog, context.getLocale(), data.getWeekEndDate());

            subtitle = Catalog.format("{0} - {1}", start, end);
        }

        params.put("subtitle", subtitle);

        setTotalExpenses(params, data);
        setBiggestPurchase(activity, params, data);
        setAdditionalSections(activity, params, data);

        return render(Template.ACTIVITIES_PERIOD_SUMMARY_HTML, params);
    }

    private void setAdditionalSections(Activity activity, Map<String, Object> params, WeeklySummaryActivityData data) {
        List<HtmlSvgSection> additionalSections = Lists.newLinkedList();
        setExpensesThisWeekChart(activity, additionalSections, data);
        setBudgetsPart(activity, additionalSections, data);

        params.put("additionalSections", additionalSections);
    }

    private void setBudgetsPart(Activity activity, List<HtmlSvgSection> additionalSections,
            WeeklySummaryActivityData data) {
        List<FollowActivityFeedbackData> followData = data.getFollowFeedback();
        if (followData == null) {
            return;
        }
        List<BudgetSummaryData> budgetSummaryDatas = Lists.newArrayList();
        Map<String, Object> params = Maps.newHashMap();

        FollowActivityFeedbackData followFeedbackData = followData.get(0);

        List<FollowItem> followItems = followFeedbackData.getFollowItems();

        if (followItems.size() > MAX_NR_BUDGET_PIES) {
            followItems = followItems.subList(0, MAX_NR_BUDGET_PIES);
        }

        int totalScreenWidth = getSvgWidth();
        int sideMargin = CONTENT_PADDING;
        int maxCircleSectionWidth = totalScreenWidth - sideMargin * 2;

        BudgetSummaryTheme budgetSummaryTheme = context.getTheme().getBudgetSummaryTheme();
        BudgetSummaryLayoutHelper layoutHelper = new BudgetSummaryLayoutHelper(budgetSummaryTheme);
        int pieRadius = layoutHelper.getPieRadius(maxCircleSectionWidth, followItems.size());
        int pieMargin = layoutHelper.getPieMargin(maxCircleSectionWidth, followItems.size(), pieRadius);

        for (FollowItem followItem : followItems) {
            BudgetSummaryData budgetSummaryData = new BudgetSummaryData();
            budgetSummaryData.setPieChart(drawPieChart(followItem, pieRadius));

            budgetSummaryData.setDeeplink(deepLinkBuilderFactory.follow(followItem.getId())
                    .withSource(getTrackingLabel(activity))
                    .build());

            budgetSummaryDatas.add(budgetSummaryData);
        }

        params.put("pieMargin", pieMargin);
        params.put("title", followFeedbackData.getTitle());
        params.put("budgets", budgetSummaryDatas);
        String chart = render(Template.ACTIVITIES_PERIOD_SUMMARIES_BUDGET_SUMMARY_HTML, params);

        HtmlSvgSection section = new HtmlSvgSection();
        section.setSvg(chart);
        section.setDeeplink(null);

        additionalSections.add(section);
    }

    private String drawPieChart(FollowItem item, int pieRadius) {
        FeedIconPieChart iconChart = new FeedIconPieChart();
        ExpensesFollowCriteria criteria = SerializationUtils.deserializeFromString(item.getCriteria(),
                ExpensesFollowCriteria.class);
        List<StringDoublePair> periodAmounts = item.getData().getPeriodAmounts();
        double filledPart = periodAmounts.get(periodAmounts.size() - 1).getValue() / criteria.getTargetAmount();

        Canvas canvas = new Canvas(pieRadius * 2, pieRadius * 2);

        int backgroundRadius = pieRadius;
        int innerBackgroundRadius = (int) (pieRadius * 0.92f);

        char icon;
        int iconSize;
        Color backgroundColor;
        Color currentAmountColor;
        Color exceededAmountColor = context.getTheme().getColor(ColorTypes.WARNING);

        if (Objects.equal(item.getType(), FollowTypes.SEARCH)) {
            if (v2) {
                icon = TinkIconUtils.IconsV2.SEARCH;
                iconSize = (int) (pieRadius * 1.10f);
            } else {
                icon = TinkIconUtils.Icons.SEARCH;
                iconSize = (int) (pieRadius * 0.9f);
            }
            backgroundColor = context.getTheme().getColor(ColorTypes.SEARCH);
            currentAmountColor = context.getTheme().getColor(ColorTypes.SEARCH_PIE_CHART);
        } else {
            if (v2) {
                icon = TinkIconUtils.getV2CategoryIcon(context.getCategory(criteria.getCategoryIds().get(0)));
                iconSize = (int) (pieRadius * 1.10f);
            } else {
                icon = TinkIconUtils.getV1CategoryIcon(context.getCategory(criteria.getCategoryIds().get(0)));
                iconSize = (int) (pieRadius * 1.25f);
            }
            backgroundColor = context.getTheme().getColor(ColorTypes.EXPENSES);
            currentAmountColor = context.getTheme().getColor(ColorTypes.EXPENSES_PIE_CHART);
        }

        setupFeedIconPieChart(iconChart, backgroundColor, currentAmountColor, exceededAmountColor, icon,
                backgroundRadius, innerBackgroundRadius, iconSize, filledPart);

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

    private void setExpensesThisWeekChart(Activity activity, List<HtmlSvgSection> additionalSections,
            WeeklySummaryActivityData data) {

        if (data.getHistoricalExpensesAverage() == null) {
            return;
        }

        List<KVPair<String, Double>> amountPerWeekday = Lists.newArrayList();
        List<KVPair<String, Double>> averageAmountPerWeekday = Lists.newArrayList();

        double max = 0d;
        KVPair<String, Double> currentWeekMax = new KVPair<>("", 0d);

        DateFormat dateFormatter = new SimpleDateFormat("EEEE", context.getLocale());

        padHistoricalExpenses(data.getHistoricalExpenses());

        for (KVPair<String, Double> d : data.getHistoricalExpenses()) {
            Date date = DateTime.parse(d.getKey()).toDate();
            String formattedDay = dateFormatter.format(date);
            if (formattedDay.length() > 3) {
                formattedDay = formattedDay.substring(0, 3);
            }

            KVPair<String, Double> currentWeekday = new KVPair<String, Double>(formattedDay, Math.abs(d.getValue()));
            amountPerWeekday.add(currentWeekday);

            Double currentWeekdayAverage = Math.abs(getDayAverage(date, data.getHistoricalExpensesAverage()));
            averageAmountPerWeekday.add(new KVPair<String, Double>(formattedDay, currentWeekdayAverage));

            if (currentWeekMax.getValue() <= currentWeekday.getValue()) {
                currentWeekMax = currentWeekday;
            }
            max = Math.max(currentWeekdayAverage, Math.max(max, currentWeekMax.getValue()));
        }

        KVPairBarChart currentWeekChart = new KVPairBarChart(context.getTheme(), context.getCatalog(),
                context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context.getLocale()),
                context.getUserAgent());
        KVPairBarChart averageWeekChart = new KVPairBarChart(context.getTheme(), context.getCatalog(),
                context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context.getLocale()),
                context.getUserAgent());

        List<KVPair<String, Double>> labelMarkedValues = Lists.newLinkedList();
        labelMarkedValues.add(currentWeekMax);

        currentWeekChart.setLabelMarkedValues(labelMarkedValues);
        Canvas c = getChartAreaCanvasInsidePadding(140);

        averageWeekChart.setBarColor(context.getTheme().getColor(ColorTypes.EXPENSES_COMPARISON));
        averageWeekChart.setLabelColor(Theme.Colors.TRANSPARENT);
        averageWeekChart.setValues(averageAmountPerWeekday);
        averageWeekChart.setMaxValue(max);
        averageWeekChart.setMinValue(0);
        averageWeekChart.setLeaveRoomForAmountLabels(true);
        averageWeekChart.setAmountLabelTextSize(12f);
        averageWeekChart.setLeftMargin(-5f);
        averageWeekChart.setBarMargin(24);
        averageWeekChart.setBarMinimumHeight(1);
        averageWeekChart.setHorizontalPadding(8);

        averageWeekChart.draw(c);

        currentWeekChart.setBarColor(context.getTheme().getColor(ColorTypes.EXPENSES));
        currentWeekChart.setLabelColor(context.getTheme().getColor(ColorTypes.CHART_AXIS_X_LABEL));
        currentWeekChart.setXAxisLabelTextSize(12);
        currentWeekChart.setValues(amountPerWeekday);
        currentWeekChart.setMaxValue(max);
        currentWeekChart.setMinValue(0);
        currentWeekChart.setLeaveRoomForAmountLabels(true);
        currentWeekChart.setAmountLabelTextSize(12f);
        currentWeekChart.setLeftMargin(5f);
        currentWeekChart.setBarMargin(24);
        currentWeekChart.setBarMinimumHeight(1);
        currentWeekChart.setHorizontalPadding(8);

        currentWeekChart.draw(c);

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("title", context.getCatalog().getString("Expenses this week").toUpperCase());
        params.put("average", context.getCatalog().getString("average"));
        params.put("svg", c.draw());
        String chart = render(Template.ACTIVITIES_PERIOD_SUMMARIES_EXPENSES_THIS_WEEK_HTML, params);
        HtmlSvgSection section = new HtmlSvgSection();
        section.setSvg(chart);

        section.setDeeplink(deepLinkBuilderFactory.search()
                .withQuery(Catalog.format(context.getCatalog().getString("Expenses Week {0}"), data.getWeek()))
                .withSource(getTrackingLabel(activity))
                .build());

        additionalSections.add(section);
    }

    /**
     * Add days for the historical expenses so that we always have data for at least one week / 7 days
     */
    private void padHistoricalExpenses(List<KVPair<String, Double>> historicalExpenses) {
        DateTime lastDate = DateTime.parse(historicalExpenses.get(historicalExpenses.size() - 1).getKey());

        int i = 1;

        while (historicalExpenses.size() < 7) {
            historicalExpenses.add(new KVPair<>(DateUtils.toDayPeriod(lastDate.plusDays(i)), 0D));
            i++;
        }
    }

    private double getDayAverage(Date date, List<KVPair<Integer, Double>> averageList) {
        Calendar c = DateUtils.getCalendar(date);
        for (KVPair<Integer, Double> average : averageList) {
            if (c.get(Calendar.DAY_OF_WEEK) == average.getKey()) {
                return average.getValue();
            }
        }
        return Double.MAX_VALUE;
    }

    private void setBiggestPurchase(Activity activity, Map<String, Object> params, WeeklySummaryActivityData data) {
        Catalog catalog = context.getCatalog();
        BiggestPurchaseData biggestPurchase = new BiggestPurchaseData();

        biggestPurchase.setTitle(catalog.getString("Biggest purchase").toUpperCase(context.getLocale()));
        Transaction biggestExpense = data.getBiggestExpense();

        // Null protections
        if (biggestExpense == null) {
            biggestPurchase.setAmount("");
            biggestPurchase.setTitle("");
            biggestPurchase.setDescription("");
            biggestPurchase.setDeeplink("");

            params.put("biggestPurchaseData", biggestPurchase);
            return;
        }

        biggestPurchase.setAmount(I18NUtils.formatCurrency(Math.abs(biggestExpense.getAmount()),
                context.getUserCurrency(), context.getLocale()));
        biggestPurchase.setDescription(biggestExpense.getDescription());

        biggestPurchase.setDeeplink(deepLinkBuilderFactory.transaction(biggestExpense.getId())
                .withSource(getTrackingLabel(activity))
                .build());

        params.put("biggestPurchaseData", biggestPurchase);
    }

    private void setTotalExpenses(Map<String, Object> params, WeeklySummaryActivityData data) {
        Catalog catalog = context.getCatalog();

        TotalExpensesData totalExpenses = new TotalExpensesData();
        totalExpenses.setTotal(I18NUtils
                .formatCurrency(Math.abs(data.getExpensesAmount()), context.getUserCurrency(), context.getLocale()));
        totalExpenses.setTitle(catalog.getString("Total expenses").toUpperCase(context.getLocale()));

        double avgDiff = data.getExpensesAverageAmount() - data.getExpensesAmount();
        if (avgDiff >= 0) {
            totalExpenses.setAverage(Catalog.format(catalog.getString("{0} over avg."),
                    I18NUtils.formatCurrency(Math.abs(avgDiff), context.getUserCurrency(), context.getLocale())));
            totalExpenses.setAverageColor(context.getTheme().getColorHex(ColorTypes.WARNING));
        } else {
            totalExpenses.setAverage(Catalog.format(catalog.getString("{0} below avg."),
                    I18NUtils.formatCurrency(Math.abs(avgDiff), context.getUserCurrency(), context.getLocale())));
            totalExpenses.setAverageColor(context.getTheme().getColorHex(ColorTypes.INCOME));
        }

        Category category = context.getExpenseCategory();
        String categoryRef;

        if (Versions.shouldUseNewFeed(context.getUserAgent(), context.getCluster())) {
            categoryRef = category.getCode();
        } else {
            categoryRef = category.getId();
        }

        totalExpenses.setDeeplink(deepLinkBuilderFactory.category(categoryRef).build());
        params.put("totalExpensesData", totalExpenses);
    }
}
