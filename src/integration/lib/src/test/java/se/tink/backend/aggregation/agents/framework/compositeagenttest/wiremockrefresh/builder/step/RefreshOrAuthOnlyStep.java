package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step;

import java.util.Set;
import se.tink.libraries.credentials.service.RefreshableItem;

public interface RefreshOrAuthOnlyStep {

    RefreshableItemStep addRefreshableItems(RefreshableItem... items);

    RefreshableItemStep withRefreshableItems(Set<RefreshableItem> refreshableItems);

    /** Test will be executed without any refreshable items */
    BuildStep testOnlyAuthentication();
}
