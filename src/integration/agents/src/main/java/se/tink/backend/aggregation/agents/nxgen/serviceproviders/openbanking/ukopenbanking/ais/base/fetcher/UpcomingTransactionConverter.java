package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public interface UpcomingTransactionConverter<ResponseType> {
    List<UpcomingTransaction> toUpcomingTransactions(ResponseType response);
}
