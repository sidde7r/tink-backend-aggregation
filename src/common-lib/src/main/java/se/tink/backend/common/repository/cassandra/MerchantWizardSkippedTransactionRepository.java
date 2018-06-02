package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.merchants.MerchantWizardSkippedTransaction;

public interface MerchantWizardSkippedTransactionRepository
        extends CassandraRepository<MerchantWizardSkippedTransaction>,
        MerchantWizardSkippedTransactionRepositoryCustom {
}

