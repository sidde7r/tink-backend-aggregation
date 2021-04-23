package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.creditcard.entities;

import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@Builder
public class CreditCardEntity {

    private String maskedCardNumber;
    private String cardNumber;
    private String description;
    private ExactCurrencyAmount disposed;
    private ExactCurrencyAmount limit;
    private ExactCurrencyAmount available;

    // fields used to generate params requests
    private String accountCode;
    private String keyPageMovs;
    private String cardCode;
    private String cardtype;
    private String codeCardType;
    private String entityCard;
    private String agreementCard;
    private String panDescription;
    private String descriptionCardType;

    public CreditCardAccount toTinkAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedCardNumber)
                                .withBalance(disposed)
                                .withAvailableCredit(available)
                                .withCardAlias(description)
                                .build())
                .withoutFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(cardNumber)
                                .withAccountNumber(cardNumber)
                                .withAccountName(description)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                            cardNumber))
                                .build())
                .build();
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }
}
