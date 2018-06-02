package se.tink.backend.common.workers.activity.renderers;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.TemplateUtils;
import se.tink.backend.core.Activity;
import se.tink.backend.core.DiscoverActivityData;

public class DiscoverActivityRenderer extends BaseActivityRenderer {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public DiscoverActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }
    
    @Override
    public String renderHtml(Activity activity) {

        DiscoverActivityData data = activity.getContent(DiscoverActivityData.class);

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("title", activity.getTitle());
        params.put("description", activity.getMessage());
        params.put("buttonLabel", data.getButtonText());
        params.put("deeplink", getDeepLink(activity));
        if (!Strings.isNullOrEmpty(data.getImage())) {
            params.put("svg", TemplateUtils.getTemplate(data.getImage()));
        }
        params.put("activityClass", "discover");

        return render(Template.ACTIVITIES_DISCOVER_HTML, params);
    }

    private String getDeepLink(Activity activity) {
        switch (activity.getType()) {
        case Activity.Types.DISCOVER_BUDGETS:
            return deepLinkBuilderFactory.follow().withSource(getTrackingLabel(activity)).build();
        case Activity.Types.DISCOVER_CATEGORIES:
            return "https://www.abnamro.nl/nl/prive/apps/grip/categorieen.html";
        default:
            return null;
        }
    }
}
