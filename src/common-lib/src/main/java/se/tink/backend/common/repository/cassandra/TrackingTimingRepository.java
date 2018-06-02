package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;

import se.tink.backend.core.tracking.TrackingTiming;

public interface TrackingTimingRepository extends CassandraRepository<TrackingTiming>, TrackingTimingRepositoryCustom {

}
