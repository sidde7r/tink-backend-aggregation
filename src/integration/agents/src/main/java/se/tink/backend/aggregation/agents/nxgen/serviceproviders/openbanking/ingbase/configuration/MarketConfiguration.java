package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration;

import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.BaseFetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

public interface MarketConfiguration {

    boolean shouldReturnLowercaseAccountId();

    LocalDate earliestTransactionHistoryDate();

    /** Class to use for transactions response. This allows to customise transaction parsing. */
    Class<? extends BaseFetchTransactionsResponse> getTransactionsResponseClass();

    List<Party> convertHolderNamesToParties(String holderNames);
}
