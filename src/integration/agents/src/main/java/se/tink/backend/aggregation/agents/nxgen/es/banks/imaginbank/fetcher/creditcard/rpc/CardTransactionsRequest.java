package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities.TransactionFilterEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionsRequest {
    @JsonProperty("filtros")
    private TransactionFilterEntity filter;

    @JsonProperty("masDatos")
    private String moreData;

    public static final CardTransactionsRequest createCardTransactionsRequest(
            boolean moreData, String filterCards, LocalDate startDate, LocalDate endDate) {
        CardTransactionsRequest request = new CardTransactionsRequest();
        request.moreData = String.valueOf(moreData);
        request.filter =
                TransactionFilterEntity.createTransactionFilter(filterCards, startDate, endDate);

        return request;
    }
}
