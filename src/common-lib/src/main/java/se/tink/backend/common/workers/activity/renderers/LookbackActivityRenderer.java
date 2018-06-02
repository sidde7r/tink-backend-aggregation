package se.tink.backend.common.workers.activity.renderers;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.core.Activity;
import se.tink.backend.core.LookbackActivityContent;
import se.tink.backend.rpc.HtmlDetailsResponse;

public class LookbackActivityRenderer extends DetailsActivityRenderer
{

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public LookbackActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        LookbackActivityContent content = activity.getContent(LookbackActivityContent.class);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("activity", activity);
        params.put("content", content);
        params.put("deeplink", deepLinkBuilderFactory.shareableHtml(activity.getId()).build());

        return render(Template.ACTIVITIES_LOOKBACK_HTML, params);
    }

    @Override
    public HtmlDetailsResponse renderDetailsHtml(Activity activity) {
        LookbackActivityContent content = activity.getContent(LookbackActivityContent.class);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("activity", activity);
        params.put("content", content);

        // TODO Click action

        HtmlDetailsResponse response = new HtmlDetailsResponse();
        response.setHtml(addDetailsFrame(render(Template.ACTIVITIES_LOOKBACK_HTML, params)));
        response.setShareableMessage(context.getCatalog().getString("Share me"));

        return response;
    }
}
