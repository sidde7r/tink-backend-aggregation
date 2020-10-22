package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.common.rpc.BaseRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequest extends BaseRequest {
    private Body body;

    public static class Body {
        @JsonProperty("numeroConto")
        private String accountNumber;

        @JsonProperty("numeroMovimentiPagina")
        private int transactionsPerPage;

        @JsonProperty("numeroPagina")
        private int page;
    }

    public TransactionsRequest(String accountNumber, int transactionNumberPage, int page) {
        this.body = new Body();
        this.body.accountNumber = accountNumber;
        this.body.transactionsPerPage = transactionNumberPage;
        this.body.page = page;
    }
}
