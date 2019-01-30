package se.tink.backend.aggregation.agents;

import se.tink.libraries.credentials.service.RefreshableItem;

public interface RefreshableItemExecutor {
    void refresh(RefreshableItem item);
}
