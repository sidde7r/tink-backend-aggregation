package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.AccountBalance;

public interface AccountBalanceHistoryRepository extends CassandraRepository<AccountBalance>,
        AccountBalanceHistoryRepositoryCustom {

}
