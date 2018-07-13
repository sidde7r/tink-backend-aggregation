package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.rpc.RefreshableItem;

public interface RefreshableItemExecutor {
    void refresh(RefreshableItem item);
}
