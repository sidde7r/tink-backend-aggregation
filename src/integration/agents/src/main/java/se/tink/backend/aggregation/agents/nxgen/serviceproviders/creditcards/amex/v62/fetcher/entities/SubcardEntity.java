package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.ZERO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class SubcardEntity {

    private String cardMemberName;
    private String cardProductName;
    private String suppIndex;

    public String getCardMemberName() {
        return cardMemberName;
    }

    public String getCardProductName() {
        return cardProductName;
    }

    public String getSuppIndex() {
        return suppIndex;
    }

    @JsonIgnore
    public CreditCardAccount toCreditCardAccount(
            final AmericanExpressV62Configuration configuration) {
        String accountNumber = transformCardNameToAccountNumber();
        return CreditCardAccount.builder(
                        accountNumber, configuration.toAmount(ZERO), configuration.toAmount(ZERO))
                .setHolderName(getHolderName())
                .setAccountNumber(accountNumber)
                .setName(cardProductName)
                .build();
    }

    @JsonIgnore
    private HolderName getHolderName() {
        return new HolderName(cardMemberName);
    }

    @JsonIgnore
    public String transformCardNameToAccountNumber() {
        String cardNumber = cardProductName.split("-")[1];
        return "XXX-" + cardNumber;
    }
}
