package se.tink.backend.abnamro.workers.activity.renderers;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.joda.time.DateTime;
import se.tink.backend.abnamro.workers.activity.generators.models.AbnAmroMonthlySummaryActivityData;
import se.tink.backend.abnamro.workers.activity.renderers.models.TransactionSummaryData;
import se.tink.backend.common.Versions;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.generators.models.FollowActivityFeedbackData;
import se.tink.backend.common.workers.activity.renderers.ActivityRendererContext;
import se.tink.backend.common.workers.activity.renderers.BaseActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.models.BudgetSummaryData;
import se.tink.backend.common.workers.activity.renderers.models.HtmlSvgSection;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.svg.charts.ChartArea.XAxisLabelPosition;
import se.tink.backend.common.workers.activity.renderers.svg.charts.ChartArea.YAxisLabelPosition;
import se.tink.backend.common.workers.activity.renderers.svg.charts.FeedIconPieChart;
import se.tink.backend.common.workers.activity.renderers.svg.charts.LeftToSpendLineChartArea;
import se.tink.backend.common.workers.activity.renderers.themes.BudgetSummaryTheme;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.LeftToSpendTheme;
import se.tink.backend.common.workers.activity.renderers.utils.BudgetSummaryLayoutHelper;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Balance;
import se.tink.backend.core.Category;
import se.tink.backend.core.Currency;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.utils.ChartUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AbnAmroMonthlySummaryActivityRenderer extends BaseActivityRenderer {

    private static final int LEFT_TO_SPEND_CHART_HEIGHT = 120;
    private static final int LEFT_TO_SPEND_CHART_Y_AXIS_LABEL_MAX_COUNT = 3;
    private static final int FOLLOW_ITEM_MAX_COUNT = 6;
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public AbnAmroMonthlySummaryActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {

        final Catalog catalog = context.getCatalog();
        final Locale locale = context.getLocale();

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("title", activity.getTitle());

        AbnAmroMonthlySummaryActivityData data = activity.getContent(AbnAmroMonthlySummaryActivityData.class);

        Period period = data.getPeriod();

        if (Objects.equal(ResolutionTypes.MONTHLY_ADJUSTED, period.getResolution())) {
            String startDate = I18NUtils.formatShortDate(catalog, locale, period.getStartDate());
            String endDate = I18NUtils.formatShortDate(catalog, locale, period.getEndDate());
            String subtitle = Catalog.format("{0} - {1}", startDate, endDate);

            params.put("subtitle", subtitle);
        }

        params.put("expenseData", getExpenseSummaryData(activity, data));
        params.put("incomeData", getIncomeSummaryData(activity, data));
        params.put("leftToSpendData", getLeftToSpendSummaryData(activity, data));

        if (data.getExpenseCount() > 0 || data.getIncomeCount() > 0) {
            params.put("leftToSpendChart", getLeftToSpendChart(activity, data));
        }

        params.put("budgetSummary", getFollowFeedbackSection(activity, data));

        return render(Template.ACTIVITIES_ABNAMRO_MONTHLY_SUMMARY_HTML, params);
    }

    private String getDescriptionOfChange(double change, String unchanged, String decreaseFormat,
            String increaseFormat) {
        final Currency currency = context.getUserCurrency();
        final Locale locale = context.getLocale();

        double approximatelyUnchanged = 10 * currency.getFactor();

        final String changeString = I18NUtils.formatCurrency(Math.abs(change), currency, locale);

        if (Math.abs(change) < approximatelyUnchanged) {
            return unchanged;
        } else if (change < 0) {
            return Catalog.format(decreaseFormat, changeString);
        } else {
            return Catalog.format(increaseFormat, changeString);
        }
    }

    private String getDescriptionOfChange(double change) {
        final Catalog catalog = context.getCatalog();
        return getDescriptionOfChange(
                change,
                catalog.getString("Same as previous month"),
                catalog.getString("{0} less than previous month"),
                catalog.getString("{0} more than previous month"));
    }

    private TransactionSummaryData getExpenseSummaryData(Activity activity, AbnAmroMonthlySummaryActivityData data) {

        final Catalog catalog = context.getCatalog();
        final Locale locale = context.getLocale();
        final Period period = data.getPeriod();

        TransactionSummaryData expenseData = new TransactionSummaryData();
        expenseData.setAmount(I18NUtils.formatCurrency(data.getExpenseAmount(), context.getUserCurrency(), locale));

        Category category = context.getExpenseCategory();
        String categoryRef;

        if (Versions.shouldUseNewFeed(context.getUserAgent(), context.getCluster())) {
            categoryRef = category.getCode();
        } else {
            categoryRef = category.getId();
        }

        expenseData.setDeeplink(deepLinkBuilderFactory.category(categoryRef)
                .withPeriod(period.getName())
                .withSource(getTrackingLabel(activity))
                .build());

        expenseData.setDescription(getDescriptionOfChange(data.getExpenseChange()));

        Icon icon = new Icon();
        icon.setColorType(Icon.IconColorTypes.EXPENSE);
        if (v2) {
            icon.setChar(TinkIconUtils.getV2CategoryIcon(context.getExpenseCategory()));
        } else {
            icon.setChar(TinkIconUtils.getV1CategoryIcon(context.getExpenseCategory()));
        }
        expenseData.setIcon(icon);

        if (data.getExpenseCount() == 1) {
            expenseData.setTitle(Catalog.format(catalog.getString("{0} expense"), data.getExpenseCount()));
        } else {
            expenseData.setTitle(Catalog.format(catalog.getString("{0} expenses"), data.getExpenseCount()));
        }

        return expenseData;
    }

    private HtmlSvgSection getFollowFeedbackSection(Activity activity, AbnAmroMonthlySummaryActivityData data) {

        final FollowActivityFeedbackData followFeedbackData = data.getFollowFeedback();
        if (followFeedbackData == null) {
            return null;
        }

        Map<String, Object> params = new HashMap<String, Object>();

        List<FollowItem> followItems = followFeedbackData.getFollowItems();

        if (followItems.size() > FOLLOW_ITEM_MAX_COUNT) {
            followItems = followItems.subList(0, FOLLOW_ITEM_MAX_COUNT);
        }

        int totalScreenWidth = getSvgWidth();
        int sideMargin = CONTENT_PADDING;
        int maxCircleSectionWidth = totalScreenWidth - sideMargin * 2;

        BudgetSummaryTheme budgetSummaryTheme = context.getTheme().getBudgetSummaryTheme();
        BudgetSummaryLayoutHelper layoutHelper = new BudgetSummaryLayoutHelper(budgetSummaryTheme);
        int pieRadius = layoutHelper.getPieRadius(maxCircleSectionWidth, followItems.size());
        int pieMargin = layoutHelper.getPieMargin(maxCircleSectionWidth, followItems.size(), pieRadius);

        List<BudgetSummaryData> budgetSummaryDatas = Lists.newArrayList();
        for (FollowItem followItem : followItems) {
            String pieChart = getFollowItemPieChartSVG(followItem, pieRadius);

            if (!Strings.isNullOrEmpty(pieChart)) {
                BudgetSummaryData budgetSummaryData = new BudgetSummaryData();
                budgetSummaryData.setPieChart(pieChart);

                budgetSummaryData.setDeeplink(deepLinkBuilderFactory.follow(followItem.getId())
                        .withPeriod(data.getPeriod().getName())
                        .withSource(getTrackingLabel(activity))
                        .build());

                budgetSummaryDatas.add(budgetSummaryData);
            }
        }
        params.put("pieMargin", pieMargin);
        params.put("title", followFeedbackData.getTitle());
        params.put("budgets", budgetSummaryDatas);
        String chart = render(Template.ACTIVITIES_PERIOD_SUMMARIES_BUDGET_SUMMARY_HTML, params);

        HtmlSvgSection section = new HtmlSvgSection();
        section.setDeeplink(null);
        section.setSvg(chart);

        return section;
    }

    private String getFollowItemPieChartSVG(FollowItem item, int pieRadius) {

        ExpensesFollowCriteria criteria = SerializationUtils.deserializeFromString(item.getCriteria(),
                ExpensesFollowCriteria.class);

        char icon;
        int iconSize;
        Color backgroundColor;
        Color currentAmountColor;

        if (Objects.equal(item.getType(), FollowTypes.SEARCH)) {
            if (v2) {
                icon = TinkIconUtils.IconsV2.SEARCH;
            } else {
                icon = TinkIconUtils.Icons.SEARCH;
            }
            iconSize = (int) (pieRadius * 0.9f);
            backgroundColor = context.getTheme().getColor(ColorTypes.SEARCH);
            currentAmountColor = context.getTheme().getColor(ColorTypes.SEARCH_PIE_CHART);
        } else {
            if (v2) {
                icon = TinkIconUtils.getV2CategoryIcon(context.getCategory(criteria.getCategoryIds().get(0)));
            } else {
                icon = TinkIconUtils.getV1CategoryIcon(context.getCategory(criteria.getCategoryIds().get(0)));
            }
            iconSize = (int) (pieRadius * 1.25f);
            backgroundColor = context.getTheme().getColor(ColorTypes.EXPENSES);
            currentAmountColor = context.getTheme().getColor(ColorTypes.EXPENSES_PIE_CHART);
        }

        int outerRadius = pieRadius;
        int innerRadius = (int) (pieRadius * 0.92f);

        List<StringDoublePair> periodAmounts = item.getData().getPeriodAmounts();
        double filledPart = periodAmounts.get(periodAmounts.size() - 1).getValue() / criteria.getTargetAmount();
        Color exceededAmountColor = context.getTheme().getColor(ColorTypes.WARNING);

        FeedIconPieChart iconChart = new FeedIconPieChart();
        setupFeedIcon(iconChart, backgroundColor, icon, outerRadius, iconSize);
        iconChart.setInnerRadius(innerRadius);
        iconChart.setCurrentAmountColor(currentAmountColor);
        iconChart.setFilledPart(filledPart);
        iconChart.setExceededAmountColor(exceededAmountColor);

        Canvas canvas = new Canvas(pieRadius * 2, pieRadius * 2);
        iconChart.draw(canvas, context.getTheme().isV2());
        return canvas.draw();
    }

    private TransactionSummaryData getIncomeSummaryData(Activity activity, AbnAmroMonthlySummaryActivityData data) {

        final Catalog catalog = context.getCatalog();
        final Locale locale = context.getLocale();
        final Period period = data.getPeriod();

        TransactionSummaryData incomeData = new TransactionSummaryData();
        incomeData.setAmount(I18NUtils.formatCurrency(data.getIncomeAmount(), context.getUserCurrency(), locale));

        Category category = context.getIncomeCategory();
        String categoryRef;

        if (Versions.shouldUseNewFeed(context.getUserAgent(), context.getCluster())) {
            categoryRef = category.getCode();
        } else {
            categoryRef = category.getId();
        }

        incomeData.setDeeplink(deepLinkBuilderFactory.category(categoryRef)
                .withPeriod(period.getName())
                .withSource(getTrackingLabel(activity))
                .build());

        incomeData.setDescription(getDescriptionOfChange(data.getIncomeChange()));

        Icon icon = new Icon();
        icon.setColorType(Icon.IconColorTypes.INCOME);
        if (v2) {
            icon.setChar(TinkIconUtils.getV2CategoryIcon(context.getIncomeCategory()));
        } else {
            icon.setChar(TinkIconUtils.getV1CategoryIcon(context.getIncomeCategory()));
        }
        incomeData.setIcon(icon);

        if (data.getIncomeCount() == 1) {
            incomeData.setTitle(Catalog.format(catalog.getString("{0} income"), data.getIncomeCount()));
        } else {
            incomeData.setTitle(Catalog.format(catalog.getString("{0} incomes"), data.getIncomeCount()));
        }

        return incomeData;
    }

    private String getLeftToSpendChart(Activity activity, AbnAmroMonthlySummaryActivityData data) {

        List<Statistic> leftToSpendStatistic = data.getLeftToSpend();

        List<Balance> balances = Lists.newArrayList(ChartUtils.LeftToSpendCharts
                .getBalancesFromStatistics(leftToSpendStatistic));

        double maxValue = Math.max(0, ChartUtils.BalanceCharts.getMax(balances));
        double minValue = Math.min(0, ChartUtils.BalanceCharts.getMin(balances));

        List<DateTime> dates = Lists.transform(balances, Balance::getDate);

        LeftToSpendLineChartArea chartArea = new LeftToSpendLineChartArea(context.getTheme(), context.getCatalog(),
                context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context.getLocale()));

        LeftToSpendTheme leftToSpendTheme = context.getTheme().getLeftToSpendTheme();

        chartArea.setAreaGradientBottomColor(leftToSpendTheme.getChartPositiveAreaGradientBottomColor());
        chartArea.setAreaGradientTopColor(leftToSpendTheme.getChartPositiveAreaGradientTopColor());
        chartArea.setBalances(balances);
        chartArea.setDates(dates);
        chartArea.setFill(true);
        chartArea.setMaxValue(maxValue);
        chartArea.setMinValue(minValue);
        chartArea.setNegativeAreaGradientBottomColor(leftToSpendTheme.getChartNegativeAreaGradientBottomColor());
        chartArea.setNegativeAreaGradientTopColor(leftToSpendTheme.getChartNegativeAreaGradientTopColor());
        chartArea.setNegativePathColor(leftToSpendTheme.getChartNegativePathColor());
        chartArea.setPathColor(leftToSpendTheme.getChartPositivePathColor());
        chartArea.setShowXAxisGridlines(leftToSpendTheme.showXAxisGridLines());
        chartArea.setXAxisLabelPosition(XAxisLabelPosition.BOTTOM_INSIDE_FOR_POSITIVE_OUTSIDE_FOR_NEGATIVE);
        chartArea.setShowYAxisGridlines(leftToSpendTheme.showYAxisGridLines());
        chartArea.setYAxisLabelPosition(YAxisLabelPosition.LEFT_INSIDE_CHARTAREA);
        chartArea.setTodayDotDiameter(leftToSpendTheme.getTodayDotDiameter());
        chartArea.setXAxisPaint(leftToSpendTheme.getXAxisColor());
        chartArea.setShowXAxis(true);
        chartArea.setDrawXAxisOnTop(true);
        chartArea.setPathStroke(leftToSpendTheme.getChartPathStroke());
        chartArea.setYAxisGridlineStroke(leftToSpendTheme.getYAxisGridLineStroke());
        chartArea.setYAxisLabelMaxCount(LEFT_TO_SPEND_CHART_Y_AXIS_LABEL_MAX_COUNT);
        chartArea.setYAxisLabelIncludeZeroLine(leftToSpendTheme.showYAxisLabelZeroLine());
        chartArea.setHighlightXAxis(leftToSpendTheme.highlightXAxis());
        chartArea.setSmooth(leftToSpendTheme.isSmooth());

        Canvas canvas = new Canvas(getSvgWidth(), LEFT_TO_SPEND_CHART_HEIGHT);
        chartArea.draw(canvas, v2);

        return canvas.draw();
    }

    private TransactionSummaryData getLeftToSpendSummaryData(Activity activity,
            AbnAmroMonthlySummaryActivityData data) {

        final Catalog catalog = context.getCatalog();
        final Locale locale = context.getLocale();
        final Period period = data.getPeriod();

        double amount = data.getLeftToSpendAmount();

        TransactionSummaryData leftToSpendData = new TransactionSummaryData();

        leftToSpendData.setAmount(Catalog.format("= {0}",
                I18NUtils.formatCurrency(amount, context.getUserCurrency(), locale)));

        leftToSpendData.setDeeplink(deepLinkBuilderFactory.leftToSpend()
                .withPeriod(period.getName())
                .withSource(getTrackingLabel(activity))
                .build());

        leftToSpendData.setDescription(getDescriptionOfChange(data.getLeftToSpendChange()));
        LeftToSpendTheme theme = context.getTheme().getLeftToSpendTheme();
        String color = amount < 0 ? theme.getNegativeIconType() : theme.getPositiveIconType();
        if (v2) {
            leftToSpendData.setIcon(getIconSVG(color, TinkIconUtils.IconsV2.SALARY));

        } else {
            leftToSpendData.setIcon(getIconSVG(color, TinkIconUtils.Icons.LEFT_TO_SPEND));
        }
        leftToSpendData.setTitle(catalog.getString("Left to Spend"));

        return leftToSpendData;
    }
}
