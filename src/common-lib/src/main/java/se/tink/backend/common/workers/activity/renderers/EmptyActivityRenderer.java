package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.common.template.Template;
import se.tink.backend.core.Activity;

/**
 * Rendering empty (non-visible) activities is a hack to force the feed to update if an activity is removed.
 * The need for this is because the feed only updates if new activities have been added. 
 */
public class EmptyActivityRenderer extends BaseActivityRenderer {

    public EmptyActivityRenderer(ActivityRendererContext context) {
        super(context);
    }

    @Override
    public String renderHtml(Activity activity) {

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);

        return render(Template.ACTIVITIES_EMPTY_HTML, params);
    }
}
