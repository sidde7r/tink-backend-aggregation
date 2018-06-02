package se.tink.backend.common.workers.activity.renderers;

import java.util.HashMap;
import java.util.Map;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Provider;

public class SuggestProviderActivityRenderer extends BaseActivityRenderer {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public SuggestProviderActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        Provider provider = activity.getContent(Provider.class);

        String deeplink = getDeepLink(activity, provider);
        String buttonLabel = context.getCatalog().getString("Add account");

        String svg = getSvg(provider);

        String message = Catalog.format(
                context.getCatalog().getString(
                        "It seems like you have an account at {0} that you haven't connected to Tink."),
               "<b>" + provider.getDisplayName() + "</b>");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("svg", svg);
        params.put("activity", activity);
        params.put("title", provider.getDisplayName());
        params.put("deeplink", deeplink);
        params.put("description", message);
        params.put("buttonLabel", buttonLabel);

        return render(Template.ACTIVITIES_SUGGEST_PROVIDER_HTML, params);
    }

    private String getSvg(Provider data) {
        char icon;
        ColorTypes color;

        if (v2) {
            icon = TinkIconUtils.IconsV2.ACCOUNTS;
            color = ColorTypes.DEFAULT;
        } else {
            icon = TinkIconUtils.Icons.ACCOUNTS;
            color = ColorTypes.INFO;
        }

        return this.getIconSVG(context.getTheme().getColor(color), icon, 25, 28);
    }

    private String getDeepLink(Activity activity, Provider provider) {
        return deepLinkBuilderFactory.addProvider(provider.getName()).withSource(getTrackingLabel(activity)).build();
    }

}
