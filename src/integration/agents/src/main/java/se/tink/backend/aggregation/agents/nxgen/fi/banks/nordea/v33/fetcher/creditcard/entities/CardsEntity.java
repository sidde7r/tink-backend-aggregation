package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CardsEntity {
    @JsonProperty("card_id")
    private String cardId;
    @JsonProperty("card_category")
    private String cardCategory;
    @JsonProperty("card_status")
    private String cardStatus;
    @JsonProperty("cardholder_name")
    private String cardholderName;
    @JsonProperty("cardholder_type")
    private String cardholderType;
    @JsonProperty("principal_cardholder_name")
    private String principalCardholderName;
    @JsonProperty("product_code")
    private String productCode;
    @JsonProperty("country_code")
    private String countryCode;
    private String currency;
    @JsonProperty("pan_id")
    private String panId;
    @JsonProperty("atm_account_number")
    private String atm_account_number;
    @JsonProperty
    private CreditEntity credit;

    public CreditCardAccount toTinkCard() {

        return CreditCardAccount
                .builder(credit.getMaskedCreditCardNumber(), new Amount(currency, credit.getCreditBookedBalance()),
                        new Amount(currency, credit.getCreditAvailableBalance()))
                .setAccountNumber(credit.getMaskedCreditCardNumber())
                .setHolderName(new HolderName(cardholderName))
                .setBankIdentifier(cardId)
                .build();
    }

    public String getCardCategory() {
        return cardCategory;
    }
}
