package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import se.tink.backend.common.health.Checkable;
import se.tink.backend.core.CassandraPeriodByUserId;
import se.tink.backend.core.CassandraTransactionByUserIdPeriod;
import se.tink.backend.core.Category;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface CassandraTransactionByUserIdAndPeriodRepositoryCustom extends Creatable {

    // The following are methods reflecting TransactionRepositoryCustom.

    void deleteByUserId(String userId);

    void deleteByUserIdAndId(String userId, String id);
    void deleteByUserIdAndPeriodAndId(String userId, int period, String id);

    void deleteByUserIdAndCredentials(String userId, String credentialsId);

    void deleteByUserIdAndAccountId(String userId, String accountId);

    CassandraTransactionByUserIdPeriod findByUserIdAndIdWithPeriod(String userId, int period, String id);

    List<CassandraTransactionByUserIdPeriod> findByUserIdAndIdsWithPeriods(String userId,
            List<String> ids, List<CassandraPeriodByUserId> periods) throws ExecutionException, InterruptedException;

    List<CassandraTransactionByUserIdPeriod> findLastYearToNextYearByUserIdWithPeriods(
            String userId, int maxPeriod) throws ExecutionException, InterruptedException;

    List<CassandraTransactionByUserIdPeriod> findByUserIdWithPeriods(String userId,
            List<Integer> periods) throws InterruptedException, ExecutionException;

    Long countByUserId(UUID userId);

    void updateUserModifiedCategory(String userId, Map<String, Integer> transactionIdsToPeriods, Category category);

    void updateMerchantIdAndDescription(String userId, Map<String, Integer> transactionIdsToPeriods,
            String merchantId,
            String merchantName);

    // Alternative #save(...) and #delete(...) (which are using Consistency.ONE).

    <S extends CassandraTransactionByUserIdPeriod> S saveByQuorum(S entity);

    <S extends CassandraTransactionByUserIdPeriod> Iterable<S> saveByQuorum(Iterable<S> entities, int batchSize);

    void deleteByQuorum(Iterable<? extends CassandraTransactionByUserIdPeriod> entities, int batchSize);
}
