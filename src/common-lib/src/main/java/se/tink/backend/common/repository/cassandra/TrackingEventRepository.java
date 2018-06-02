package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;

import se.tink.backend.core.tracking.TrackingEvent;

public interface TrackingEventRepository extends CassandraRepository<TrackingEvent>, TrackingEventRepositoryCustom {

}
