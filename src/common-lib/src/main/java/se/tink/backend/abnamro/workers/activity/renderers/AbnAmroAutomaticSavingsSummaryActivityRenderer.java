package se.tink.backend.abnamro.workers.activity.renderers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import se.tink.backend.abnamro.workers.activity.generators.models.AbnAmroAutomaticSavingsSummaryActivityData;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.renderers.ActivityRendererContext;
import se.tink.backend.common.workers.activity.renderers.BaseActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.models.ActivityHeader;
import se.tink.backend.common.workers.activity.renderers.models.FeedTransactionData;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Transaction;

public class AbnAmroAutomaticSavingsSummaryActivityRenderer extends BaseActivityRenderer {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public AbnAmroAutomaticSavingsSummaryActivityRenderer(ActivityRendererContext context,
            DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {

        AbnAmroAutomaticSavingsSummaryActivityData data = activity
                .getContent(AbnAmroAutomaticSavingsSummaryActivityData.class);

        ActivityHeader headerData = new ActivityHeader();
        headerData.setDeepLink(getDeepLink(activity));
        headerData.setIcon(getIconForActivity(activity));
        headerData.setLeftHeader(activity.getTitle());
        headerData.setLeftSubtext(activity.getMessage());
        headerData.setRightHeader(I18NUtils.formatCurrency(data.getAmount(), context.getUserCurrency(),
                context.getLocale()));
        headerData.setRightSubtext(getDateSpan(data));

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("headerData", headerData);
        params.put("innerTemplate", "transaction-plural");
        params.put("transactions", getTransactionRows(data.getTransactions(), activity));

        return render(Template.ACTIVITIES_TRANSACTION_PLURAL_COLLAPSED_HTML, params);
    }

    private String getDateSpan(AbnAmroAutomaticSavingsSummaryActivityData data) {
        if (I18NUtils.isSameDay(data.getStartDate(), data.getEndDate())) {
            return I18NUtils.humanDateFormat(context.getCatalog(), context.getLocale(), data.getStartDate());
        } else {
            return Catalog.format("{0} - {1}",
                    I18NUtils.humanDateShortFormat(context.getCatalog(), context.getLocale(), data.getStartDate()),
                    I18NUtils.humanDateShortFormat(context.getCatalog(), context.getLocale(), data.getEndDate()));
        }
    }

    private String getDeepLink(Activity activity) {
        return deepLinkBuilderFactory.tracking().withSource(getTrackingLabel(activity)).build();
    }

    private Icon getIconForActivity(Activity a) {
        Icon icon = new Icon();
        icon.setColorType(Icon.IconColorTypes.TRANSFER);
        if (v2) {
            icon.setChar(TinkIconUtils.IconsV2.TRANSFER);
        } else {
            icon.setChar(TinkIconUtils.Icons.TRANSFER);
        }
        return icon;
    }

    private Icon getIconForTransaction(Transaction t, Activity activity) {
        Icon icon = new Icon();
        icon.setColorType(Icon.IconColorTypes.TRANSFER);
        if (v2) {
            icon.setChar(TinkIconUtils.IconsV2.TRANSFER);
        } else {
            icon.setChar(TinkIconUtils.Icons.TRANSFER);
        }
        return icon;
    }

    private String getTransactionDeepLink(Transaction transaction, Activity activity) {
        return deepLinkBuilderFactory.transaction(transaction.getId()).withSource(getTrackingLabel(activity)).build();
    }

    private FeedTransactionData getTransactionHeaderData(Transaction t, Activity activity) {
        final Catalog catalog = context.getCatalog();

        FeedTransactionData headerData = new FeedTransactionData();
        headerData.setDeepLink(getTransactionDeepLink(t, activity));
        headerData.setIcon(getIconForTransaction(t, activity));
        headerData.setLeftHeader(t.getDescription());
        headerData.setLeftSubtext(context.getCategory(t.getCategoryId()).getDisplayName());
        headerData.setRightHeader(I18NUtils.formatCurrency(t.getAmount(), context.getUserCurrency(),
                context.getLocale()));
        headerData.setRightSubtext(I18NUtils.humanDateFormat(catalog, context.getLocale(), t.getDate()));
        headerData.setTransaction(t);

        return headerData;
    }

    private List<FeedTransactionData> getTransactionRows(List<Transaction> transactions, Activity activity) {
        List<FeedTransactionData> transactionsRows = Lists.newArrayList();

        for (Transaction transaction : transactions) {
            transactionsRows.add(getTransactionHeaderData(transaction, activity));
        }

        return transactionsRows;
    }
}
