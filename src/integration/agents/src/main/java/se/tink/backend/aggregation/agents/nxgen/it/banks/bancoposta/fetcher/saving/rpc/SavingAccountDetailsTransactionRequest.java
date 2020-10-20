package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.common.rpc.BaseRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingAccountDetailsTransactionRequest extends BaseRequest {
    private Body body;

    @JsonObject
    public static class Body {
        @JsonProperty("numeroRapporto")
        private String accountNumber;

        @JsonProperty("numMovimenti")
        private int transactionsPerPage = 40;
    }

    public SavingAccountDetailsTransactionRequest(String accountNumber) {
        this.body = new Body();
        this.body.accountNumber = accountNumber;
    }
}
