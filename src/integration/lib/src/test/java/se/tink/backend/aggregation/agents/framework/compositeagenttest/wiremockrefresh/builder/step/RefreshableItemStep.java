package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step;

import java.util.Set;
import se.tink.libraries.credentials.service.RefreshableItem;

public interface RefreshableItemStep extends BuildStep {

    RefreshableItemStep addRefreshableItems(RefreshableItem... items);

    RefreshableItemStep withRefreshableItems(Set<RefreshableItem> refreshableItems);
}
