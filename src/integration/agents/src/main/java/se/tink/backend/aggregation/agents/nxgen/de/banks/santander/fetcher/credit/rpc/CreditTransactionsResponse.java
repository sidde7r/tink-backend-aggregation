package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities.CreditMethodResult;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CreditTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    @JsonProperty("methodResult")
    private CreditMethodResult creditMethodResult;

    @Override
    public String nextKey() {
        return null;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return creditMethodResult.getTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(!creditMethodResult.getIsLastPage());
    }
}
