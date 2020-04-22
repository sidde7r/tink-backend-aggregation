package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.BaseFetchTransactionsResponse;

public interface AspspConfiguration {

    /** Class to use for transactions response. This allows to customise transaction parsing. */
    Class<? extends BaseFetchTransactionsResponse> getTransactionsResponseClass();
}
