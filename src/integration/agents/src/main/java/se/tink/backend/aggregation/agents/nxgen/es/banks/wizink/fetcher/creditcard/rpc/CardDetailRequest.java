package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class CardDetailRequest {

    public CardDetailRequest(String accountNumber, String cardNumber) {
        this.requestCardDetailRequestBody = new CardDetailRequestBody(accountNumber, cardNumber);
    }

    @JsonProperty("CardDetailRequest")
    private CardDetailRequestBody requestCardDetailRequestBody;

    @JsonObject
    private static class CardDetailRequestBody {

        private String accountNumber;
        private boolean actualizeMutableData;
        private String cardNumber;

        private CardDetailRequestBody(String accountNumber, String cardNumber) {
            this.accountNumber = accountNumber;
            this.cardNumber = cardNumber;
            this.actualizeMutableData = false;
        }
    }
}
