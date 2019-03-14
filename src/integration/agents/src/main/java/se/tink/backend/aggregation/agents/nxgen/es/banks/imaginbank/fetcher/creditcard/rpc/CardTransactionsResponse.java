package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities.CardTransactionsListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardTransactionsResponse {
    @JsonProperty("listaMovimientos")
    private CardTransactionsListEntity transactionsList;
    @JsonProperty("totalMovimientosEncontrados")
    private int totalNumberOfTransactions;

    @JsonIgnore
    public PaginatorResponse toPaginatorResponse() {
        List<CreditCardTransaction> transactions = transactionsList.getTinkTransactions();
        return PaginatorResponseImpl.create(transactions, transactionsList.canFetchMore());
    }
}
