package se.tink.backend.common.workers.activity.renderers;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.awt.Color;
import java.awt.Stroke;
import java.util.ArrayList;
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
import se.tink.backend.common.workers.activity.generators.models.MonthlySummaryActivityData;
import se.tink.backend.common.workers.activity.renderers.models.BiggestPurchaseData;
import se.tink.backend.common.workers.activity.renderers.models.BudgetSummaryData;
import se.tink.backend.common.workers.activity.renderers.models.HtmlSvgSection;
import se.tink.backend.common.workers.activity.renderers.models.TotalExpensesData;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.svg.charts.ChartArea.XAxisLabelPosition;
import se.tink.backend.common.workers.activity.renderers.svg.charts.FeedIconPieChart;
import se.tink.backend.common.workers.activity.renderers.svg.charts.KVPairBarChart;
import se.tink.backend.common.workers.activity.renderers.svg.charts.LeftToSpendLineChartArea;
import se.tink.backend.common.workers.activity.renderers.svg.charts.LeftToSpendTransactionOverlay;
import se.tink.backend.common.workers.activity.renderers.themes.BudgetSummaryTheme;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.common.workers.activity.renderers.utils.BudgetSummaryLayoutHelper;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Balance;
import se.tink.backend.core.Category;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.LeftToSpendBalance;
import se.tink.backend.core.MonthlySummaryActivityCategoryData;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.utils.ChartUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class MonthlySummaryActivityRenderer extends BaseActivityRenderer {

    private static final int MAX_NR_BUDGET_PIES = 6;
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public MonthlySummaryActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("activity", activity);

        MonthlySummaryActivityData data = activity.getContent(MonthlySummaryActivityData.class);
        setPeriod(params, activity);
        setTotalExpenses(activity, params, data);
        setBiggestPurchase(activity, params, data);
        setAdditionalSections(activity, params, data);

        return render(Template.ACTIVITIES_PERIOD_SUMMARY_HTML, params);
    }

    private void setPeriod(Map<String, Object> params, Activity activity) {
        params.put("period", StringUtils.firstLetterUppercaseFormatting(activity.getTitle()));
    }

    private void setAdditionalSections(Activity activity, Map<String, Object> params, MonthlySummaryActivityData data) {
        List<HtmlSvgSection> additionalSections = Lists.newLinkedList();
        setLeftToSpendChart(activity, additionalSections, data);
        setUnusualCategoriesChart(activity, additionalSections, data);
        setBudgetsPart(activity, additionalSections, data);
        params.put("additionalSections", additionalSections);
    }

    private void setBiggestPurchase(Activity activity, Map<String, Object> params, MonthlySummaryActivityData data) {
        Catalog catalog = context.getCatalog();

        BiggestPurchaseData biggestPurchase = new BiggestPurchaseData();

        List<Transaction> largestExpenses = data.getLargestExpenses();
        // Null protections
        if (largestExpenses == null || largestExpenses.size() == 0) {
            biggestPurchase.setAmount("");
            biggestPurchase.setTitle("");
            biggestPurchase.setDescription("");
            biggestPurchase.setDeeplink("");

            params.put("biggestPurchaseData", biggestPurchase);
            return;
        }
        Transaction largestExpense = largestExpenses.get(0);

        biggestPurchase.setTitle(catalog.getString("Biggest purchase").toUpperCase(context.getLocale()));
        biggestPurchase.setAmount(I18NUtils.formatCurrency(Math.abs(largestExpense.getAmount()),
                context.getUserCurrency(), context.getLocale()));

        biggestPurchase.setDescription(largestExpense.getDescription());
        biggestPurchase.setDeeplink(deepLinkBuilderFactory.transaction(largestExpense.getId())
                .withSource(getTrackingLabel(activity))
                .build());

        params.put("biggestPurchaseData", biggestPurchase);
    }

    private void setTotalExpenses(Activity activity, Map<String, Object> params, MonthlySummaryActivityData data) {

        Catalog catalog = context.getCatalog();
        TotalExpensesData totalExpenses = new TotalExpensesData();
        double expenses = Math.abs(data.getExpenses());

        totalExpenses.setTotal(I18NUtils.formatCurrency(expenses, context.getUserCurrency(), context.getLocale()));
        totalExpenses.setTitle(catalog.getString("Total expenses").toUpperCase(context.getLocale()));

        double avg = expenses - Math.abs(data.getExpensesAvg());
        if (avg < 0) {
            totalExpenses.setAverage(Catalog.format(catalog.getString("{0} below avg."),
                    I18NUtils.formatCurrency(Math.abs(avg), context.getUserCurrency(), context.getLocale())));
            totalExpenses.setAverageColor(context.getTheme().getColorHex(ColorTypes.INCOME));
        } else {
            totalExpenses.setAverage(Catalog.format(catalog.getString("{0} over avg."),
                    I18NUtils.formatCurrency(Math.abs(avg), context.getUserCurrency(), context.getLocale())));
            totalExpenses.setAverageColor(context.getTheme().getColorHex(ColorTypes.WARNING));

        }

        Category category = context.getExpenseCategory();
        String categoryRef;

        if (Versions.shouldUseNewFeed(context.getUserAgent(), context.getCluster())) {
            categoryRef = category.getCode();
        } else {
            categoryRef = category.getId();
        }

        totalExpenses.setDeeplink(deepLinkBuilderFactory.category(categoryRef)
                .withPeriod(data.getPeriod())
                .withSource(getTrackingLabel(activity))
                .build());

        params.put("totalExpensesData", totalExpenses);
    }

    private void setLeftToSpendChart(Activity activity, List<HtmlSvgSection> additionalSections,
            MonthlySummaryActivityData data) {

        LeftToSpendTransactionOverlay averageChart = new LeftToSpendTransactionOverlay(context.getTheme(),
                context.getCatalog(), context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context
                        .getLocale()), context.getCategories(), context.getUserAgent());
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

        Stroke avgStroke = Theme.Strokes.DASH_STROKE;

        averageChart.setSmooth(true);
        averageChart.setPathStroke(avgStroke);
        averageChart.setPathColor(context.getTheme().getColor(ColorTypes.COMPARISON));
        averageChart.setNegativePathColor(context.getTheme().getColor(ColorTypes.COMPARISON));

        averageChart.setYAxisLabelPaint(context.getTheme().getColor(ColorTypes.CHART_AXIS_Y_LABEL));

        if (ChartUtils.BalanceCharts.getMin(new ArrayList<Balance>(balances)) > 0) {
            // Do nothing
        } else {
            averageChart.setShowXAxis(true);
            averageChart.setXAxisPaint(v2 ? context.getTheme().getColor(ColorTypes.DEFAULT) : context.getTheme().getColor(ColorTypes.CRITICAL));
            averageChart.setXAxisStroke(Theme.Strokes.DASH_STROKE);
        }

        List<KVPair<Balance, Transaction>> balanceTransactionPairs = getBalanceTransactionPairs(
                data.getLargestExpenses(), data.getLargestIncome(), balances);

        averageChart.setBalanceTransactionPairs(balanceTransactionPairs);

        LeftToSpendTransactionOverlay currentChart = new LeftToSpendTransactionOverlay(context.getTheme(),
                context.getCatalog(), context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context
                        .getLocale()), context.getCategories(), context.getUserAgent());

        currentChart.setNegativePathColor(v2 ? context.getTheme().getColor(ColorTypes.DEFAULT) : context.getTheme().getColor(ColorTypes.CRITICAL));
        currentChart.setFill(true);
        currentChart.setMarkToday(true);
        currentChart.setXAxisLabelPosition(XAxisLabelPosition.NONE);
        currentChart.setXAxisLabelPaint(Theme.Colors.TRANSPARENT);
        currentChart.setTodayDotDiameter(4);
        currentChart.setShowXAxisGridlines(false);

        ArrayList<DateTime> dates = new ArrayList<DateTime>();
        for (Balance balance : meanBalances) {
            dates.add(balance.getDate());
        }

        if (!dates.isEmpty()) {
            updateChart(currentChart, balances, minValue, maxValue, dates);
            updateChart(averageChart, meanBalances, minValue, maxValue, dates);

            Map<String, Object> params = new HashMap<String, Object>();
            currentChart.draw(canvas, v2);
            averageChart.draw(canvas, v2);

            params.put("svg", canvas.draw());
            String chart = render(Template.ACTIVITIES_PERIOD_SUMMARIES_LEFT_TO_SPEND_CHART_HTML,
                    params);

            HtmlSvgSection section = new HtmlSvgSection();

            section.setDeeplink(deepLinkBuilderFactory.leftToSpend()
                    .withPeriod(data.getPeriod())
                    .withSource(getTrackingLabel(activity))
                    .build());

            section.setSvg(chart);

            additionalSections.add(section);
        }

    }

    private List<KVPair<Balance, Transaction>> getBalanceTransactionPairs(

            List<Transaction> expenses, List<Transaction> income, List<LeftToSpendBalance> balances) {
        List<KVPair<Balance, Transaction>> result = Lists.newArrayList();
        if (expenses == null || income == null || balances == null || balances.size() == 0) {
            return result;
        }
        List<DateTime> addedDates = Lists.newArrayList();
        DateTime lastDate = balances.get(balances.size() - 1).getDate();

        // Add income
        if (income != null && income.size() != 0) {
            Transaction firstIncome = income.get(0);
            for (Balance balance : balances) {
                if (DateUtils.isSameDay(income.get(0).getDate(), balance.getDate().toDate())) {
                    result.add(new KVPair<Balance, Transaction>(balance, firstIncome));
                    addedDates.add(balance.getDate());
                    break;
                }
            }
        }

        for (Transaction transaction : expenses) {
            for (Balance balance : balances) {
                if (DateUtils.isSameDay(transaction.getDate(), balance.getDate().toDate())
                        && !adjacentToDates(addedDates, balance.getDate())
                        && Math.abs(DateUtils.daysBetween(balance.getDate(), lastDate)) >= 4) {
                    result.add(new KVPair<Balance, Transaction>(balance, transaction));
                    addedDates.add(balance.getDate());
                    break;
                }
            }
        }
        return result;
    }

    private boolean adjacentToDates(List<DateTime> list, DateTime dateToCheck) {
        for (DateTime date : list) {
            if (Math.abs(DateUtils.daysBetween(date, dateToCheck)) < 6) {
                return true;
            }
        }
        return false;
    }

    protected void updateChart(LeftToSpendLineChartArea chartArea, List<LeftToSpendBalance> balances, double minValue,
            double maxValue, List<DateTime> dates) {
        chartArea.setDates(dates);
        chartArea.setMaxValue(maxValue);
        chartArea.setMinValue(minValue);
        chartArea.setBalances(new ArrayList<>(balances));
    }

    private void setBudgetsPart(Activity activity, List<HtmlSvgSection> additionalSections,
            MonthlySummaryActivityData data) {
        List<FollowActivityFeedbackData> followData = data.getFollowFeedback();
        if (followData == null) {
            return;
        }
        List<BudgetSummaryData> budgetSummaryDatas = Lists.newArrayList();
        Map<String, Object> params = new HashMap<String, Object>();

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
                    .withPeriod(data.getPeriod())
                    .withSource(getTrackingLabel(activity))
                    .build());

            budgetSummaryDatas.add(budgetSummaryData);
        }

        params.put("pieMargin", pieMargin);
        params.put("title", followFeedbackData.getTitle());
        params.put("budgets", budgetSummaryDatas);
        String chart = render(Template.ACTIVITIES_PERIOD_SUMMARIES_BUDGET_SUMMARY_HTML, params);

        HtmlSvgSection section = new HtmlSvgSection();
        section.setDeeplink(null);
        section.setSvg(chart);

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

    private void setUnusualCategoriesChart(Activity activity, List<HtmlSvgSection> additionalSections,
            MonthlySummaryActivityData data) {
        List<MonthlySummaryActivityCategoryData> largestCategories = data.getUnusualSpending();

        if (largestCategories == null) {
            return;
        }

        double largestCategory = 0;

        List<KVPair<String, Double>> amounts = Lists.newArrayList();
        List<KVPair<String, Double>> averageAmounts = Lists.newArrayList();
        List<KVPair<String, Double>> labelMarkedValues = Lists.newLinkedList();

        for (MonthlySummaryActivityCategoryData summaryData : largestCategories) {
            largestCategory = Math.max(Math.max(Math.abs(summaryData.getAverage()), Math.abs(summaryData.getAmount())),
                    largestCategory);

            String categoryDisplayName = context.getCategory(summaryData.getCategoryId()).getDisplayName();

            categoryDisplayName = categoryDisplayName.replaceAll("& ", "&\\\n");

            amounts.add(new KVPair<String, Double>(categoryDisplayName, Math.abs(summaryData.getAmount())));
            averageAmounts.add(new KVPair<String, Double>(categoryDisplayName, Math.abs(summaryData.getAverage())));
            labelMarkedValues.add(new KVPair<String, Double>(categoryDisplayName, Math.abs(summaryData.getAmount())));
        }

        KVPairBarChart currentWeekChart = new KVPairBarChart(context.getTheme(), context.getCatalog(),
                context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context.getLocale()),
                context.getUserAgent());
        KVPairBarChart averageWeekChart = new KVPairBarChart(context.getTheme(), context.getCatalog(),
                context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context.getLocale()),
                context.getUserAgent());

        Canvas c = getChartAreaCanvasInsidePadding(150);

        averageWeekChart.setBarColor(context.getTheme().getColor(ColorTypes.EXPENSES_COMPARISON));
        averageWeekChart.setLabelColor(Theme.Colors.TRANSPARENT);
        averageWeekChart.setXAxisLabelTextSize(12);
        averageWeekChart.setValues(averageAmounts);
        averageWeekChart.setMaxValue(largestCategory);
        averageWeekChart.setMinValue(0);
        averageWeekChart.setLeaveRoomForAmountLabels(true);
        averageWeekChart.setAmountLabelTextSize(12f);
        averageWeekChart.setLeftMargin(-8f);
        averageWeekChart.setBarMargin(this.getSvgWidth() * 0.22f);
        averageWeekChart.setBarMinimumHeight(1);
        averageWeekChart.setHorizontalPadding(36f);
        averageWeekChart.draw(c);

        currentWeekChart.setLabelMarkedValues(labelMarkedValues);

        currentWeekChart.setBarColor(context.getTheme().getColor(ColorTypes.EXPENSES));
        currentWeekChart.setLabelColor(context.getTheme().getColor(ColorTypes.CHART_AXIS_X_LABEL_HIGHLIGHTED));
        currentWeekChart.setXAxisLabelTextSize(12);
        currentWeekChart.setValues(amounts);
        currentWeekChart.setMaxValue(largestCategory);
        currentWeekChart.setMinValue(0);
        currentWeekChart.setLeaveRoomForAmountLabels(true);
        currentWeekChart.setAmountLabelTextSize(12f);
        currentWeekChart.setLeftMargin(8f);
        currentWeekChart.setBarMargin(this.getSvgWidth() * 0.22f);
        currentWeekChart.setBarMinimumHeight(1);
        currentWeekChart.setHorizontalPadding(36f);

        currentWeekChart.draw(c);

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("title", context.getCatalog().getString("Unusual spending").toUpperCase(context.getLocale()));
        params.put("average", context.getCatalog().getString("normally"));
        params.put("svg", c.draw());

        HtmlSvgSection section = new HtmlSvgSection();

        Category category = context.getExpenseCategory();
        String categoryRef;

        if (Versions.shouldUseNewFeed(context.getUserAgent(), context.getCluster())) {
            categoryRef = category.getCode();
        } else {
            categoryRef = category.getId();
        }

        section.setDeeplink(deepLinkBuilderFactory.category(categoryRef)
                .withPeriod(data.getPeriod())
                .withSource(getTrackingLabel(activity))
                .build());

        section.setSvg(render(Template.ACTIVITIES_PERIOD_SUMMARIES_UNUSUAL_SPENDING_HTML, params));

        additionalSections.add(section);
    }
}
