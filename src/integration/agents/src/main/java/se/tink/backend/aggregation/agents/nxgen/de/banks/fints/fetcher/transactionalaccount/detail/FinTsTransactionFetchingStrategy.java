package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.detail;

import java.util.List;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction.TransactionClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.FinTsTransactionMapper;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@AllArgsConstructor
public abstract class FinTsTransactionFetchingStrategy {
    protected TransactionClient transactionClient;
    protected FinTsTransactionMapper mapper;
    protected int version;

    public abstract List<AggregationTransaction> execute(FinTsAccountInformation account);
}
