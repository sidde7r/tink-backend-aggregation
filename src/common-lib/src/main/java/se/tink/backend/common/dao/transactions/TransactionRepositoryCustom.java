package se.tink.backend.common.dao.transactions;

import java.util.List;
import java.util.Map;
import se.tink.backend.common.health.Checkable;
import se.tink.backend.core.Transaction;

public interface TransactionRepositoryCustom extends Checkable {

    void updateMerchantIdAndDescription(String userId, Map<String, Integer> transactionIdsToPeriods,
            String merchantId,
            String merchantName);

    void deleteByUserIdAndCredentials(String userId, String credentialsId);

    void deleteByUserIdAndAccountId(String userId, String accountId);

    void deleteByUserId(String userId);

    void deleteByUserIdAndId(String userId, String id);

}
