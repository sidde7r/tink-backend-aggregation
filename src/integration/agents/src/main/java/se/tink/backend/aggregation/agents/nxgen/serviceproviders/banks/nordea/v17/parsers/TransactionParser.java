package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.parsers;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.libraries.amount.Amount;

public interface TransactionParser {
    Amount getAmount(TransactionEntity te);

    Date getDate(TransactionEntity te);

    String getDescription(TransactionEntity te);

    boolean isPending(TransactionEntity te);

    Amount getAmount(PaymentEntity pe);

    Date getDate(PaymentEntity pe);

    String getDescription(PaymentEntity pe);
}
