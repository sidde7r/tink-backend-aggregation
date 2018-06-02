package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;

import se.tink.backend.core.tracking.TrackingView;

public interface TrackingViewRepository extends CassandraRepository<TrackingView>, TrackingViewRepositoryCustom {

}
