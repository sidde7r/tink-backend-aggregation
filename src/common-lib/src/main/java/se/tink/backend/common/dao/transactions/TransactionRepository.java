package se.tink.backend.common.dao.transactions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;

@Repository
@Transactional
public interface TransactionRepository extends TransactionRepositoryCustom {
    List<Transaction> findAllByUserId(String userId);

    List<Transaction> findByUserIdAndIds(String userId, List<String> ids);

    List<Transaction> findByUserIdAndTime(String userId, DateTime startDate, DateTime endDate);

    Optional<Transaction> findOneOptionallyByUserIdAndId(String userId, int period, String transactionId);

    Transaction findByUserIdAndId(String userId, int period, String transactionId);

    Transaction findOneByUserIdAndIds(String userId, String transactionId);

    int countByUserId(String userId);

    void updateCategory(String userId, Map<String, Integer> transactionIdsToPeriods,
            Category category);

    void save(Iterable<Transaction> transactions);

    void save(Transaction t);

    void deleteAll();

    void delete(Iterable<Transaction> transactions);
}
