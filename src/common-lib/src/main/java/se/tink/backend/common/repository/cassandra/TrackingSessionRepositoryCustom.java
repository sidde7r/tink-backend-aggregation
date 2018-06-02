package se.tink.backend.common.repository.cassandra;

import java.util.UUID;

import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.tracking.TrackingSession;

public interface TrackingSessionRepositoryCustom extends Creatable {
    TrackingSession findOne(UUID id);
}
