package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngElement;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public interface IngTransactionMapper<A extends Account> {
    Transaction toTinkTransaction(A account, IngElement rawTransaction);
}
