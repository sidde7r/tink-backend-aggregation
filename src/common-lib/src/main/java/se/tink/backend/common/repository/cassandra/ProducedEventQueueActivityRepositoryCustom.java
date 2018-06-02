package se.tink.backend.common.repository.cassandra;

import java.util.List;
import se.tink.backend.core.ProducedEventQueueActivity;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface ProducedEventQueueActivityRepositoryCustom extends Creatable {
    List<ProducedEventQueueActivity> findByUserId(String userId);
}
