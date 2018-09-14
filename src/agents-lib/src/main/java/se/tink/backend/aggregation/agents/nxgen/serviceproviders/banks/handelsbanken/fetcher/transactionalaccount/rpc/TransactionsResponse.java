package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.PagableResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

public abstract class TransactionsResponse extends PagableResponse {
    public abstract List<Transaction> toTinkTransactions();

    @Override
    public Optional<URL> getPaginationKey() {
        return searchLink(HandelsbankenConstants.URLS.Links.NEXT_TRANSACTIONS);
    }
}
