package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.application.ApplicationEvent;

public interface ApplicationEventRepositoryCustom extends Creatable {
    List<ApplicationEvent> findAllByUserId(UUID userId);
    List<ApplicationEvent> findMostRecentByUserId(UUID userId, int limit);
    List<ApplicationEvent> findMostRecentByUserIdAndApplicationId(UUID userId, UUID applicationId, int limit);
}
