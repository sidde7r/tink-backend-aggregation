package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.Maps;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.renderers.models.ActivityHeader;
import se.tink.backend.common.workers.activity.renderers.models.FeedLoanData;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.core.Activity;
import se.tink.backend.core.LoanEvent;
import se.tink.backend.core.LoanEventActivityData;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.i18n.Catalog;

public class LoanEventActivityRenderer extends BaseActivityRenderer {
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public LoanEventActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        LoanEventActivityData content = activity.getContent(LoanEventActivityData.class);

        ActivityHeader headerData = new ActivityHeader();
        Icon iconSvg = getIconSVG(getIconColor(activity.getType()), TinkIconUtils.Icons.BACK_ARROW);
        headerData.setIcon(iconSvg);
        headerData.setLeftHeader(activity.getTitle());


        headerData.setLeftSubtext(activity.getMessage() + " " + generateMonthlyValueChangeMessage(content));
        headerData.setRightHeader(getInterestRateChangeHeader(content.getInterestRateChange()));
        headerData.setRightSubtext(getRateMessage(content));

        List<LoanEvent> loanEvents = content.getLoanEvents();
        List<FeedLoanData> feedLoansData = Lists.newArrayList();
        for (LoanEvent loanEvent : loanEvents) {
            feedLoansData.add(getLoanHeaderData(loanEvent, activity));
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("headerData", headerData);
        params.put("loanEvents", feedLoansData);
        params.put("deeplink", getDeepLink());

        if (content.isChallengeLoanEligible()) {
            params.put("buttonLabel", context.getCatalog().getString("Challenge your mortgage"));
        }

        return render(Template.ACTIVITIES_LOAN_EVENT_HTML, params);
    }

    private String getIconColor(String activityType) {
        switch (activityType) {
        case Activity.Types.LOAN_INCREASE:
            return Icon.IconColorTypes.CRITICAL;
        case Activity.Types.LOAN_DECREASE:
            return Icon.IconColorTypes.POSITIVE;
        default:
            throw new IllegalArgumentException(String.format("No icon color defined for type '%s'.", activityType));
        }
    }

    private  String getInterestRateChangeHeader(Double interestRateChange) {
        String operator = interestRateChange > 0 ? "+" : "";
        return operator + formatAsPercentageString(interestRateChange, true);
    }

    private String getRateMessage(LoanEventActivityData content) {
        return Catalog.format(context.getCatalog().getString(getLoanTypeMessage(content.getLoanType())),
                formatAsPercentageString(content.getCurrentInterestRate(), true));
    }

    private String getLoanTypeMessage(String loanType) {
        switch (loanType) {
        case "MORTGAGE":
            return context.getCatalog().getString("Rate {0}");
        default:
            throw new IllegalArgumentException(
                    String.format("No activity rendering defined for loan type '%s'.", loanType));
        }
    }

    private String formatAsPercentageString(Double value, boolean includePercentSign) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance(context.getLocale());
        percentFormat.setRoundingMode(RoundingMode.HALF_DOWN);
        percentFormat.setMaximumFractionDigits(3);
        percentFormat.setMinimumFractionDigits(2);

        String formattedValue = percentFormat.format(value);
        return includePercentSign ? formattedValue : formattedValue.replace("%", "");
    }

    private String getDeepLink() {
        return deepLinkBuilderFactory.createApplication(ApplicationType.SWITCH_MORTGAGE_PROVIDER.toString()).build();
    }

    private FeedLoanData getLoanHeaderData(LoanEvent loanEvent, Activity activity) {

        Icon icon = getIconSVG(getIconColor(activity.getType()), TinkIconUtils.Icons.BACK_ARROW);

        FeedLoanData headerData = new FeedLoanData();
        headerData.setLeftHeader(generateLeftSubHeaderMessage(loanEvent));
        headerData.setRightHeader(getInterestRateChangeHeader(loanEvent.getInterestRateChange()));

        LoanEventActivityData loanEventData = new LoanEventActivityData();
                loanEventData.setCurrentInterestRate(loanEvent.getInterestRateChange() + loanEvent.getInterest());
                loanEventData.setLoanType(loanEvent.getLoanType().toString());

        headerData.setRightSubtext(getRateMessage(loanEventData));
        headerData.setLeftSubtext(dateChangeMessage(loanEvent));

        headerData.setIcon(icon);
        headerData.setLoanEvent(loanEvent);
        headerData.setDeepLink(getDeepLink());

        return headerData;
    }

    private String generateLeftSubHeaderMessage(LoanEvent event) {
        String formattedCurrency = I18NUtils.formatCurrency(event.getBalance(), context.getUserCurrency(), context.getLocale());
        return String.format(context.getCatalog().getString("Balance: %s"), formattedCurrency);
    }

    private String dateChangeMessage(LoanEvent event){
        LocalDate localDate = LocalDate.from(event.getTimestamp().toInstant().atZone(ZoneId.of("CET")));
        String formattedDate = localDate.format(DateTimeFormatter.ISO_DATE);
        return Catalog.format(context.getCatalog().getString("Date of change: {0}"), formattedDate);
    }

    private String generateMonthlyValueChangeMessage(LoanEventActivityData content) {
        Double monthlyValueChange = Math.abs(content.getBalance()) / 12 * content.getInterestRateChange();
        String moreOrLess = monthlyValueChange >= 0 ? "more" : "less";
        String formattedCurrency = I18NUtils.formatCurrency(Math.abs(monthlyValueChange), context.getUserCurrency(), context.getLocale());
        String catalogString = "Mortgage: %s " + moreOrLess + " to pay per month.";
        return String.format(context.getCatalog().getString(catalogString), formattedCurrency, moreOrLess);
    }
}
