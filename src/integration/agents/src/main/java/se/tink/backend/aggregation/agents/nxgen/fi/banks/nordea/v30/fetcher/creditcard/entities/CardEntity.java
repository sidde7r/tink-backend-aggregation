package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CardEntity {

    @JsonProperty("card_id")
    private String cardId;

    @JsonProperty("card_category")
    private String cardCategory;

    @JsonProperty("card_status")
    private String cardStatus;

    private String nickname;

    @JsonProperty("cardholder_name")
    private String cardholderName;

    @JsonProperty("cardholder_type")
    private String cardholderType;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("country_code")
    private String countryCode;

    private String currency;

    @JsonProperty("atm_account_number")
    private String atmAccountNumber;

    private CreditEntity credit;

    public CreditCardAccount toTinkCard() {

        return CreditCardAccount.builder(
                credit.getMaskedCreditCardNumber(),
                new Amount(currency, credit.getSignedBalance()),
                new Amount(currency, credit.getAvailableCredit()))
                .setAccountNumber(credit.getMaskedCreditCardNumber())
                .setHolderName(new HolderName(cardholderName))
                .setName(nickname)
                .setBankIdentifier(cardId)
                .build();
    }

    public boolean isCreditCard(){
        return credit != null;
    }
}
