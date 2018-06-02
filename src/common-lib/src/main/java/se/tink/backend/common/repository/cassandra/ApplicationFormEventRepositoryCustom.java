package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.application.ApplicationFormEvent;

public interface ApplicationFormEventRepositoryCustom extends Creatable {
    List<ApplicationFormEvent> findAllByUserIdAndApplicationId(UUID userId, UUID applicationId);
}
