package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Slf4j
public class CreditCardDetailsResponse {
    private String currency;
    @Getter private String nickname;
    @Getter private String cardId;

    @JsonProperty("credit")
    private CreditDetails creditDetails;

    @JsonObject
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    private static class CreditDetails {
        private BigDecimal creditAvailableBalance;
        private BigDecimal creditBookedBalance;
        private String maskedCreditCardNumber;
    }

    public ExactCurrencyAmount getBookedBalance() {
        return ExactCurrencyAmount.of(creditDetails.getCreditBookedBalance(), currency);
    }

    public ExactCurrencyAmount getAvailableBalance() {
        return ExactCurrencyAmount.of(creditDetails.getCreditAvailableBalance(), currency);
    }

    public String getMaskedCreditCardNumber() {
        return creditDetails.maskedCreditCardNumber;
    }
}
