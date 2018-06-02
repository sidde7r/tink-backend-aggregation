package se.tink.backend.common.repository.cassandra;

import se.tink.backend.core.signableoperation.SignableOperation;

import org.springframework.data.cassandra.repository.CassandraRepository;


public interface SignableOperationRepository extends CassandraRepository<SignableOperation>, SignableOperationRepositoryCustom {

}
