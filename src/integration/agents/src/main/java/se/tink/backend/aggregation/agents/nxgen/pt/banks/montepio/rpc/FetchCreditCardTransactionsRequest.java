package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCreditCardTransactionsRequest extends FetchTransactionsRequest {

    @JsonProperty("indiceCartao")
    private String accountHandle;

    public FetchCreditCardTransactionsRequest(
            int currentPageNumber, LocalDate dateTo, LocalDate dateFrom, String accountHandle) {
        super(currentPageNumber, dateTo, dateFrom);
        this.accountHandle = accountHandle;
    }
}
