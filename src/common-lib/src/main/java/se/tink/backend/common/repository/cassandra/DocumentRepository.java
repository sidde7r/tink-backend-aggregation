package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.CompressedDocument;

public interface DocumentRepository extends CassandraRepository<CompressedDocument>, DocumentRepositoryCustom {
}
