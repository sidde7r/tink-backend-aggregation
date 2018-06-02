package se.tink.backend.common.workers.activity.renderers;

import se.tink.libraries.i18n.Catalog;
import se.tink.backend.core.Activity;
import se.tink.backend.rpc.HtmlDetailsResponse;

public abstract class DetailsActivityRenderer extends BaseActivityRenderer {
    public DetailsActivityRenderer(ActivityRendererContext context) {
        super(context);
    }

    public abstract HtmlDetailsResponse renderDetailsHtml(Activity activity);

    protected String addDetailsFrame(String html)
    {
        return Catalog.format("<div class='shareable-frame'>{0}</div>", html);
    }
}
