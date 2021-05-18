package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CardsEntity {
    private String cardId;
    @Getter private String cardCategory;
    private String cardStatus;
    private String cardholderName;
    private String cardholderType;
    private String principalCardholderName;
    private String productCode;
    private String countryCode;
    private String currency;
    private String panId;
    private String atmAccountNumber;
    private CreditEntity credit;

    public CreditCardAccount toTinkCard() {

        return CreditCardAccount.builder(
                        credit.getMaskedCreditCardNumber(),
                        ExactCurrencyAmount.of(credit.getCreditBookedBalance(), currency),
                        ExactCurrencyAmount.of(credit.getCreditAvailableBalance(), currency))
                .setAccountNumber(credit.getMaskedCreditCardNumber())
                .setHolderName(new HolderName(cardholderName))
                .setBankIdentifier(cardId)
                .build();
    }
}
