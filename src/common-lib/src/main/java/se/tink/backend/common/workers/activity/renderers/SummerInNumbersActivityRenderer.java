package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.core.Activity;

public class SummerInNumbersActivityRenderer extends BaseActivityRenderer {
    private final String S3_PATH = "https://s3-eu-west-1.amazonaws.com/tink-web/activities/summer-in-numbers";
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public SummerInNumbersActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("deeplink", deepLinkBuilderFactory.campaign().withType("pre-summer-17").build());
        params.put("title", context.getCatalog().getString("See your summer spendings"));
        params.put("imageUrl", getImageUrl(context.getUser().getLocale()));

        return render(Template.ACTIVITIES_SUMMER_IN_NUMBERS_HTML, params);
    }

    private String getImageUrl(String locale) {
        String imagePath;

        if ("sv_SE".equalsIgnoreCase(locale)) {
            imagePath = "summer-in-numbers-sv.jpg";
        } else {
            imagePath = "summer-in-numbers-en.jpg";
        }

        return String.format("%s/%s", S3_PATH, imagePath);
    }
}
