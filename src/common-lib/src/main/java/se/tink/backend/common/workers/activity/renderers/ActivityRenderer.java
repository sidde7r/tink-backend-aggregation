package se.tink.backend.common.workers.activity.renderers;

import se.tink.backend.core.Activity;

public interface ActivityRenderer {

    public String renderHtml(Activity activity);
    
}
