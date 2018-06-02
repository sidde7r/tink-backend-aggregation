package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;

public class ReimbursementActivityRenderer extends BaseActivityRenderer {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;
    private final Catalog catalog;

    public ReimbursementActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
        this.catalog = context.getCatalog();
    }

    @Override
    public String renderHtml(Activity activity) {
        Transaction transaction = activity.getContent(Transaction.class);
        Icon icon = getIconSVGForActivity(transaction);

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("icon", icon);
        params.put("title", activity.getTitle());
        params.put("message", activity.getMessage());
        params.put("buttonLabelYes", catalog.getString("Deduct from expense"));
        params.put("deeplinkYes", getTransactionDeepLink(transaction, activity));
        params.put("buttonLabelNo", catalog.getString("Skip"));
        params.put("deeplinkNo", getDeepLinkNo(transaction, activity));

        return render(Template.ACTIVITIES_REIMBURSEMENT_SWISH_SINGLE_HTML, params);
    }

    private String getDeepLinkNo(Transaction t, Activity activity) {
        return deepLinkBuilderFactory.transactionSkipLink(t.getId()).withSource(getTrackingLabel(activity)).build();
    }

    private String getTransactionDeepLink(Transaction t, Activity activity) {
        return deepLinkBuilderFactory.transaction(t.getId()).withSource(getTrackingLabel(activity)).build();
    }

    private Icon getIconSVGForActivity(Transaction transaction) {
        Icon icon = new Icon();
        icon.setColorType(Icon.IconColorTypes.INCOME);
        char rawIcon;
        if (v2) {
            rawIcon = TinkIconUtils.getV2CategoryIcon(getCategoryCode(transaction.getCategoryId()));
        } else {
            rawIcon = TinkIconUtils.getV1CategoryIcon(getCategoryCode(transaction.getCategoryId()));
        }
        icon.setChar(rawIcon);
        return icon;
    }

    private String getCategoryCode(String id) {
        for (Category c : context.getCategories()) {
            if (c.getId().equals(id)) {
                return c.getCode();
            }
        }

        return null;
    }
}
