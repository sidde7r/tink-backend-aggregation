package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.EmojiUtils;
import se.tink.backend.common.workers.activity.renderers.models.RateThisAppContent;
import se.tink.backend.core.Activity;
import se.tink.backend.core.UserState;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.enums.RateThisAppStatus;

public class RateThisAppActivityRenderer extends BaseActivityRenderer {

    private final UserStateRepository userStateRepository;
    private final Catalog catalog;

    public RateThisAppActivityRenderer(ActivityRendererContext context, UserStateRepository userStateRepository) {
        super(context);
        this.userStateRepository = userStateRepository;
        this.catalog = context.getCatalog();
    }

    @Override
    public String renderHtml(Activity activity) {
        Map<String, Object> params = Maps.newHashMap();

        params.put("activity", activity);

        if (context.getCluster() == Cluster.ABNAMRO) {
            params.put("title", catalog.getString("Do you like Grip?"));
            params.put("description", catalog.getString(
                    "We are curious to know what you think about the app. Would you like to rate it?"));
        } else {
            params.put("title", catalog.getString("Do you like Tink?") + " " + EmojiUtils.HEART);
            if (context.getUserAgent().isIOS()) {
                params.put("description", catalog.getString("Rate the app and leave a comment in the App Store!"));
            } else if (context.getUserAgent().isAndroid()) {
                params.put("description", catalog.getString("Rate the app and leave a comment in the Play Store!"));
            }
        }

        params.put("ignoreButtonLabel", context.getCatalog().getString("No, thanks"));
        params.put("rateButtonLabel", context.getCatalog().getString("Sure!"));
        params.put("content", activity.getContent(RateThisAppContent.class));

        UserState userState = userStateRepository.findOneByUserId(context.getUser().getId());

        if (Objects.equals(userState.getRateThisAppStatus(), RateThisAppStatus.NOT_SENT)) {
            userState.setRateThisAppStatus(RateThisAppStatus.SENT);
            userStateRepository.save(userState);
        }

        return render(Template.ACTIVITIES_RATE_THIS_APP_HTML, params);
    }
}
