package se.tink.backend.common.workers.activity.renderers;

import java.util.HashMap;
import java.util.Map;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.TemplateUtils;
import se.tink.backend.core.Activity;
import se.tink.backend.core.FraudDetailsActivityData;

public class FraudActivityRenderer extends BaseActivityRenderer {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public FraudActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {

        FraudDetailsActivityData data = activity.getContent(FraudDetailsActivityData.class);

        String deeplink = getDeepLink(activity);
        String buttonLabel = context.getCatalog().getPluralString("Handle warning", "Handle warnings",
                data.getUnhandledDetailsCount());

        String svg;

        if (v2) {
            svg = TemplateUtils.getTemplate("data/images/svg/fraud-shield-v2.svg");
        } else {
            svg = TemplateUtils.getTemplate("data/images/svg/fraud-shield.svg");
        }

        int count = data.getUnhandledDetailsCount();
        svg = Catalog.format(svg,
                ((count < 100) ? 18 : 14), count);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("svg", svg);
        params.put("activity", activity);
        params.put("title", activity.getTitle());
        params.put("deeplink", deeplink);
        params.put("description", activity.getMessage());
        params.put("buttonLabel", buttonLabel);

        return render(Template.ACTIVITIES_FRAUD_HTML, params);
    }

    private String getDeepLink(Activity activity) {
        return deepLinkBuilderFactory.fraud().withSource(getTrackingLabel(activity)).build();
    }
}
