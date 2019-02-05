package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.entities.AccountTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AccountTransactionsResponse extends ArrayList<AccountTransactionEntity> {

    @JsonIgnore
    public List<Transaction> getTinkTransactions() {
        return stream()
                .map(AccountTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
