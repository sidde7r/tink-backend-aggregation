package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.common.workers.activity.generators.EInvoicesActivityData;
import se.tink.backend.common.workers.activity.renderers.models.ActivityHeader;
import se.tink.backend.common.workers.activity.renderers.models.FeedTransferData;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.common.workers.activity.renderers.utils.UUIDConverter;
import se.tink.backend.core.Activity;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class EInvoicesActivityRenderer extends BaseActivityRenderer {

    private static final UUIDConverter UUID_CONVERTER = new UUIDConverter();
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public EInvoicesActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        EInvoicesActivityData data = activity.getContent(EInvoicesActivityData.class);

        if (data.getTransfers().size() > 1) {
            return renderPlural(activity);
        } else {
            return renderSingle(activity);
        }
    }

    private String renderSingle(Activity activity) {
        EInvoicesActivityData data = activity.getContent(EInvoicesActivityData.class);

        Transfer transfer = data.getTransfers().get(0);

        String deeplink = getDeepLink(activity, transfer);
        String buttonLabel = context.getCatalog().getString("Approve");

        String description = getTransferSourceMessage(transfer);
        String amount = I18NUtils.formatCurrency(
                transfer.getAmount().getValue(), context.getUserCurrency(), context.getLocale());

        String subDescription = String.format("%s, %s", amount, getTransferSubDescription(transfer));

        Icon icon = getIcon();

        Map<String, Object> params = Maps.newHashMap();
        params.put("icon", icon);
        params.put("activity", activity);
        params.put("title", activity.getTitle());
        params.put("deeplink", deeplink);
        params.put("description", description);
        params.put("subDescription", subDescription);
        params.put("buttonLabel", buttonLabel);
        params.put("transferId", UUIDUtils.toTinkUUID(transfer.getId()));

        return render(Template.ACTIVITIES_E_INVOICES_SINGLE_HTML, params);
    }

    private String getTransferSubDescription(Transfer transfer) {
        return Catalog.format(
                context.getCatalog().getString("To be payed {0}"),
                transfer.getDueDate() != null ? ThreadSafeDateFormat.FORMATTER_DAILY_PRETTY.format(transfer
                        .getDueDate()) : context.getCatalog().getString("Immediate"));
    }

    private String getTransferSourceMessage(Transfer transfer) {
        return StringUtils.formatHuman(transfer.getSourceMessage());
    }

    private String renderPlural(Activity activity) {
        EInvoicesActivityData data = activity.getContent(EInvoicesActivityData.class);
        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);

        ActivityHeader headerData = new ActivityHeader();
        Icon iconSvg = getIcon();

        List<Transfer> transferList = data.getTransfers();
        List<FeedTransferData> transferRows = Lists.newArrayList();
        StringBuilder descriptions = new StringBuilder();

        int count = transferList.size();

        for (int i = 0; i < count; i++) {
            Transfer t = transferList.get(i);
            transferRows.add(getTransferHeaderData(t, activity));

            descriptions.append(getTransferSourceMessage(t));
            if (i != count - 1) {
                descriptions.append(", ");
            }
        }

        headerData.setLeftHeader(activity.getTitle());
        headerData.setLeftSubtext(descriptions.toString());
        headerData.setIcon(iconSvg);

        params.put("headerData", headerData);
        params.put("transfers", transferRows);
        params.put("uuidConverter", UUID_CONVERTER);

        return render(Template.ACTIVITIES_E_INVOICES_PLURAL_COLLAPSED_HTML, params);
    }

    private String getDeepLink(Activity activity, Transfer transfer) {
        return deepLinkBuilderFactory.eInvoices(UUIDUtils.toTinkUUID(transfer.getId()))
                .withSource(getTrackingLabel(activity))
                .build();
    }

    private Icon getIcon() {
        Icon icon = new Icon();

        if (v2) {
            icon.setColorType(Icon.IconColorTypes.GREY);
            icon.setChar(TinkIconUtils.IconsV2.PAYBILLS);
        } else {
            icon.setColorType(Icon.IconColorTypes.POSITIVE);
            icon.setChar(TinkIconUtils.Icons.EINVOICE);
        }

        return icon;
    }

    private FeedTransferData getTransferHeaderData(Transfer transfer, Activity activity) {
        Icon icon = getIcon();

        String amount = I18NUtils.formatCurrency(
                transfer.getAmount().getValue(), context.getUserCurrency(), context.getLocale());

        FeedTransferData headerData = new FeedTransferData();
        headerData.setLeftHeader(getTransferSourceMessage(transfer));
        headerData.setRightHeader(context.getCatalog().getString("Approve"));
        headerData.setLeftSubtext(String.format("%s, %s", amount, getTransferSubDescription(transfer)));
        headerData.setIcon(icon);
        headerData.setTransfer(transfer);
        headerData.setDeepLink(getDeepLink(activity, transfer));

        return headerData;
    }
}
