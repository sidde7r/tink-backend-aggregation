package se.tink.backend.common.repository.cassandra;

import java.util.List;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.merchants.MerchantWizardSkippedTransaction;

public interface MerchantWizardSkippedTransactionRepositoryCustom extends Creatable {
    Iterable<MerchantWizardSkippedTransaction> saveInBatches(Iterable<MerchantWizardSkippedTransaction> entities);

    void deleteByUserId(String userId);

    List<MerchantWizardSkippedTransaction> findAllByUserId(String userId);
}

