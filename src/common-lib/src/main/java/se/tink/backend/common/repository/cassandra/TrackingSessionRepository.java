package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;

import se.tink.backend.core.tracking.TrackingSession;

public interface TrackingSessionRepository extends CassandraRepository<TrackingSession>,
        TrackingSessionRepositoryCustom {

}
