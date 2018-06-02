package se.tink.backend.abnamro.workers.activity.renderers;

import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.workers.activity.renderers.ActivityRendererContext;
import se.tink.backend.common.workers.activity.renderers.BaseActivityRenderer;
import se.tink.backend.core.Activity;

public class AbnAmroMaintenanceActivityRenderer extends BaseActivityRenderer {

    public AbnAmroMaintenanceActivityRenderer(ActivityRendererContext context) {
        super(context);
    }

    @Override
    public String renderHtml(Activity activity) {

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("title", activity.getTitle());
        params.put("description", activity.getMessage());

        return render(Template.ACTIVITIES_MAINTENANCE_HTML, params);
    }
}
