package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.CategoryChangeRecord;

public interface CategoryChangeRecordRepository
        extends CassandraRepository<CategoryChangeRecord>, CategoryChangeRecordRepositoryCustom {

}
