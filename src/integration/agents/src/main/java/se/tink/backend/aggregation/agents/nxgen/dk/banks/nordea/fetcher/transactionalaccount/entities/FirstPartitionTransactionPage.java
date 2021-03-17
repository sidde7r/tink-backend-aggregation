package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.TransactionsResponse;

@AllArgsConstructor
@Getter
public class FirstPartitionTransactionPage {
    private TransactionsResponse transactionsResponse;
    private Pair<String, String> partitionPeriod;
}
