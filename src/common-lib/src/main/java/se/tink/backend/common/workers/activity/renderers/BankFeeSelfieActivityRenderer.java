package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.util.List;
import java.util.Map;
import org.joda.time.Days;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.renderers.models.ActivityHeader;
import se.tink.backend.common.workers.activity.renderers.models.BankFeeSelfieData;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.svg.charts.KVPairBarChart;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Activity;
import se.tink.backend.core.BankFeeType;
import se.tink.backend.core.Currency;
import se.tink.backend.core.KVPair;
import se.tink.backend.rpc.HtmlDetailsResponse;
import se.tink.libraries.date.DateUtils;

public class BankFeeSelfieActivityRenderer extends DetailsActivityRenderer {

    private static Ordering<KVPair<String, Double>> VALUE_ORDER = new Ordering<KVPair<String, Double>>() {
        @Override
        public int compare(KVPair<String, Double> left, KVPair<String, Double> right) {
            return right.getValue().compareTo(left.getValue());
        }
    };
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public BankFeeSelfieActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {

        BankFeeSelfieData data = activity.getContent(BankFeeSelfieData.class);

        if (data == null || data.isEmpty() || data.isNewestTransactionOlderThan(Days.days(3))) {
            return null;
        }

        Map<String, Object> content = getRenderingContent(activity, data, 150);
        content.put("isSharableMode", false);

        return render(Template.ACTIVITIES_BANK_FEE_SELFIE_HTML, content);
    }

    @Override
    public HtmlDetailsResponse renderDetailsHtml(Activity activity) {
        BankFeeSelfieData data = activity.getContent(BankFeeSelfieData.class);

        Map<String, Object> content = getRenderingContent(activity, data, 200);
        content.put("isSharableMode", true);

        HtmlDetailsResponse response = new HtmlDetailsResponse();
        response.setHtml(addDetailsFrame(render(Template.ACTIVITIES_BANK_FEE_SELFIE_HTML, content)));
        response.setShareableMessage(context.getCatalog().getString("Share me"));

        return response;
    }

    private Map<String, Object> getRenderingContent(Activity activity, BankFeeSelfieData selfieData, int chartHeight) {

        String svg = getSvgChart(selfieData.getSpendingByType(), chartHeight);

        Map<String, Object> content = Maps.newHashMap();
        content.put("headerData", getHeaderData());
        content.put("selfieData", selfieData);
        content.put("activity", activity);
        content.put("svg", svg);
        content.put("message", createMessage(selfieData.getTotal(), selfieData.getAverageSpendingInTink()));
        content.put("deeplink", deepLinkBuilderFactory.shareableHtml(activity.getId()).build());

        return content;
    }

    private ActivityHeader getHeaderData() {
        ActivityHeader data = new ActivityHeader();

        Icon icon = new Icon();
        icon.setColorType(Icon.IconColorTypes.CRITICAL);
        if (v2) {
            icon.setChar(TinkIconUtils.IconsV2.ALERT);
        } else {
            icon.setChar(TinkIconUtils.Icons.BANK_FEE);
        }
        data.setIcon(icon);

        return data;
    }

    private String getSvgChart(Map<BankFeeType, Double> input, int height) {
        Canvas canvas = getStandardChartAreaCanvas(height);

        List<KVPair<String, Double>> list = FluentIterable.from(input.entrySet())
                .filter(entry -> entry.getValue() > 0)
                .transform(entry -> new KVPair<>(translateBankFeeType(entry.getKey()), entry.getValue())).toList();

        KVPairBarChart chart = new KVPairBarChart(context.getTheme(), context.getCatalog(), context.getUserCurrency(),
                context.getLocale(), DateUtils.getCalendar(context.getLocale()), context.getUserAgent());

        double max = VALUE_ORDER.min(list).getValue();
        float barMargin = calculateBarMargin(list.size());

        chart.setBarColor(context.getTheme().getColor(ColorTypes.BANK_FEE));
        chart.setLabelColor(Theme.Colors.WHITE);
        chart.setLabelMarkedColor(Theme.Colors.WHITE);
        chart.setLabelBold(false);
        chart.setXAxisLabelTextSize(12);
        chart.setLabelMarkedValues(list);
        chart.setAmountLabelTextSize(14f);
        chart.setValues(list);
        chart.setMinValue(0);
        chart.setMaxValue(max);
        chart.setLeaveRoomForAmountLabels(true);
        chart.setBarMargin(barMargin);
        chart.setLeftMargin(0);
        chart.setLabelMarkedValuesCurrencyFormat(I18NUtils.CurrencyFormat.SYMBOL);
        chart.setCornerRadius(5);
        chart.setHorizontalPadding(calculateChartPadding(list.size()));
        chart.draw(canvas);

        return canvas.draw();
    }

    private String translateBankFeeType(BankFeeType type) {
        Catalog catalog = context.getCatalog();

        switch (type) {
        case CARD_FEE:
            return catalog.getString("Card fees");
        case CASH_WITHDRAWAL_FEE:
            return catalog.getString("Cash withdrawal fees");
        case ADMINISTRATION_FEE:
            return catalog.getString("Administration fees");
        }
        return null;
    }

    private String createMessage(double userFees, double tinkAverageUserFees) {
        Catalog catalog = context.getCatalog();
        Currency currency = context.getUserCurrency();

        long difference = Math.round(userFees - tinkAverageUserFees);

        String format = "<strong>%s</strong>";

        String userAmountFormatted = String.format(format, I18NUtils.formatCurrencyRound(userFees, currency,
                context.getLocale()));
        String differenceFormatted = String.format(format, I18NUtils.formatCurrencyRound(Math.abs(difference), currency,
                context.getLocale()));

        if (difference == 0) {
            return Catalog.format(catalog.getString(
                            "You have payed at least {0} in bank fees so far this year. That is equal to the average Tink user."),
                    userAmountFormatted, differenceFormatted);
        } else if (difference > 0) {
            return Catalog.format(catalog.getString(
                            "You have payed at least {0} in bank fees so far this year. That is {1} more than the average Tink user."),
                    userAmountFormatted, differenceFormatted);
        } else {
            return Catalog.format(catalog.getString(
                            "You have payed at least {0} in bank fees so far this year. That is {1} less than the average Tink user."),
                    userAmountFormatted, differenceFormatted);
        }
    }
    
    /**
     * Calculates a good chart padding depending on the number of bars that will be displayed
     */
    private float calculateChartPadding(int numberOfBars) {
        switch (numberOfBars) {
        case 1:
            return getSvgWidth() * 0.45f;
        case 2:
            return getSvgWidth() * 0.26f;
        case 3:
        default:
            return getSvgWidth() * 0.125f;
        }
    }

    /**
     * Calculates a good bar margin depending on the number of bars that will be displayed.
     */
    private float calculateBarMargin(int numberOfBars) {
        switch (numberOfBars) {
        case 1:
            return 150;
        case 2:
            return getSvgWidth() * 0.28f;
        case 3:
        default:
            return getSvgWidth() * 0.22f;
        }
    }

}
