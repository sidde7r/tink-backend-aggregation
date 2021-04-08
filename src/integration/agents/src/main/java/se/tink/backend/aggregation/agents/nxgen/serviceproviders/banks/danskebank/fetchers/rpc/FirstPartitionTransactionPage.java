package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

@RequiredArgsConstructor
@Getter
public class FirstPartitionTransactionPage {
    private final ListTransactionsRequest transactionsRequest;
    private final ListTransactionsResponse transactionsResponse;
    private final Pair<String, String> partitionPeriod;
}
