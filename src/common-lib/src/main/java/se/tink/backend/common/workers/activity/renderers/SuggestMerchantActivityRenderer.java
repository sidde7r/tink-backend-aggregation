package se.tink.backend.common.workers.activity.renderers;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.TemplateUtils;
import se.tink.backend.core.Activity;
import se.tink.backend.core.SuggestMerchantsActivityData;

public class SuggestMerchantActivityRenderer extends BaseActivityRenderer {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public SuggestMerchantActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        SuggestMerchantsActivityData data = activity.getContent(SuggestMerchantsActivityData.class);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("activity", activity);
        params.put("title", activity.getTitle());
        params.put("description", activity.getMessage());
        params.put("buttonLabel", context.getCatalog().getString("Start wizard now"));
        params.put("deeplink", getDeepLink(activity, data.getClusterCategoryId()));
        params.put("svg", TemplateUtils.getTemplate("data/images/svg/merchant-suggest.svg"));
        params.put("activityClass", "suggest-merchant");

        return render(Template.ACTIVITIES_SUGGEST_MERCHANT_HTML, params);
    }

    private String getDeepLink(Activity activity, String categoryId) {
        return deepLinkBuilderFactory.suggestMerchant(categoryId).withSource(getTrackingLabel(activity)).build();
    }
}
