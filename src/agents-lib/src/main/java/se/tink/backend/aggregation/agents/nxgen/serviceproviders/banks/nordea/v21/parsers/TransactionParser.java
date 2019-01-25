package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.parsers;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.libraries.amount.Amount;

public interface TransactionParser {
    Amount getAmount(TransactionEntity transactionEntity);
    Date getDate(TransactionEntity transactionEntity);
    String getDescription(TransactionEntity transactionEntity);
    boolean isPending(TransactionEntity transactionEntity);

    Amount getAmount(PaymentEntity paymentEntity);
    Date getDate(PaymentEntity paymentEntity);
    String getDescription(PaymentEntity paymentEntity);
}
