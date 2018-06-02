package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.core.Activity;

public class YearInNumbersActivityRenderer extends BaseActivityRenderer {
    private final String S3_PATH = "https://s3-eu-west-1.amazonaws.com/tink-web/activities/year-in-numbers";
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public YearInNumbersActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("deeplink", deepLinkBuilderFactory.campaign().withType("year-in-numbers").build());
        params.put("title", context.getCatalog().getString("See your year in numbers!"));

        String imagePath;

        if ("sv_SE".equalsIgnoreCase(context.getUser().getLocale())) {
            imagePath = "year-in-numbers-sv.gif";
        } else {
            imagePath = "year-in-numbers-en.gif";
        }

        params.put("imageUrl", String.format("%s/%s", S3_PATH, imagePath));

        return render(Template.ACTIVITIES_YEAR_IN_NUMBERS_HTML, params);
    }
}
