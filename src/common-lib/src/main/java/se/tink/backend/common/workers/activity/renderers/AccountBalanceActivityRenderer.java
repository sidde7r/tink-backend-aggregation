package se.tink.backend.common.workers.activity.renderers;

import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.generators.models.AccountBalanceActivityData;
import se.tink.backend.common.workers.activity.renderers.models.ActivityHeader;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.svg.charts.BalanceLineChartArea;
import se.tink.backend.common.workers.activity.renderers.svg.charts.ChartArea.XAxisLabelPosition;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Balance;
import se.tink.backend.core.KVPair;
import se.tink.backend.utils.ChartUtils;
import se.tink.libraries.date.DateUtils;

public class AccountBalanceActivityRenderer extends BaseActivityRenderer {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public AccountBalanceActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {

        final AccountBalanceActivityData data = activity.getContent(AccountBalanceActivityData.class);
        List<KVPair<String, Double>> kvpairs = data.getData();

        String svg = getSvgChart(activity);

        Optional<Account> account = Optional.ofNullable(data.getAccount());
        
        if (!account.isPresent()) {
            return null;
        }

        ActivityHeader headerData = new ActivityHeader();
        headerData.setRightHeader(I18NUtils.formatCurrency(Math.round(kvpairs.get(kvpairs.size() - 1).getValue()),
                context.getUserCurrency(), context.getLocale()));
        char iconChar;
        if (v2) {
            iconChar = TinkIconUtils.IconsV2.ALERT;
        } else {
            iconChar = TinkIconUtils.Icons.ACCOUNTS;
        }
        String iconColor;

        if (activity.getType().equals(Activity.Types.BALANCE_HIGH)) {
            headerData.setLeftHeader(context.getCatalog().getString("High balance"));
            iconColor = Icon.IconColorTypes.INCOME;
        } else { // if(activity.getType().equals(Activity.Types.BALANCE))
            headerData.setLeftHeader(context.getCatalog().getString("Low balance"));
            iconColor = Icon.IconColorTypes.CRITICAL;
        }

        Icon icon = getIconSVG(iconColor, iconChar);

        headerData.setIcon(icon);
        headerData.setLeftSubtext(account.get().getName());
        headerData.setRightSubtext(I18NUtils.humanDateFormat(context.getCatalog(), context.getLocale(),
                activity.getDate()));

        headerData.setDeepLink(getDeepLink(activity, account.get()));

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("activity", activity);
        params.put("headerData", headerData);
        params.put("innerTemplate", "inner-line-chart");
        params.put("svg", svg);

        return render(Template.ACTIVITIES_BALANCE_HTML, params);
    }

    private String getDeepLink(Activity activity, Account account) {
        return deepLinkBuilderFactory.account(account.getId()).withSource(getTrackingLabel(activity)).build();
    }

    private String getSvgChart(Activity activity) {

        final AccountBalanceActivityData data = activity.getContent(AccountBalanceActivityData.class);
        List<KVPair<String, Double>> kvpairs = data.getData();

        BalanceLineChartArea chart = new BalanceLineChartArea(context.getTheme(), context.getCatalog(),
                context.getUserCurrency(), context.getLocale(), DateUtils.getCalendar(context.getLocale()));
        chart.setFill(true);
        chart.setShowXAxisGridlines(false);
        chart.setXAxisLabelPosition(XAxisLabelPosition.BOTTOM_OUTSIDE_CHARTAREA);
        chart.setYAxisLabelPaint(context.getTheme().getColor(ColorTypes.CHART_AXIS_Y_LABEL));
        chart.setXAxisLabelPaint(context.getTheme().getColor(ColorTypes.CHART_AXIS_X_LABEL));

        if (activity.getType().equals(Activity.Types.BALANCE_LOW)) {
            Stroke xAxisNegativeValueStroke = Theme.Strokes.DASH_STROKE;

            chart.setXAxisPaint(context.getTheme().getColor(ColorTypes.CRITICAL));
            chart.setXAxisStroke(xAxisNegativeValueStroke);
        } else {
            chart.setXAxisPaint(context.getTheme().getColor(ColorTypes.CHART_AXIS, Theme.Alpha.P50));
        }

        chart.setShowXAxis(true);

        List<Balance> chartData = new ArrayList<Balance>();
        for (KVPair<String, Double> kvpair : kvpairs) {
            chartData.add(new Balance(kvpair));
        }

        Canvas c = new Canvas(getSvgWidth(), 150);

        chart.setDates(ChartUtils.BalanceCharts.getDatesFromBalances(chartData));
        chart.setBalances(chartData);
        chart.setMaxValue(Math.max(0, ChartUtils.BalanceCharts.getMax(chartData)) * 1.1);
        chart.setMinValue(Math.min(0, ChartUtils.BalanceCharts.getMin(chartData)) * 1.1);
        chart.draw(c, v2);

        return c.draw();
    }
}
