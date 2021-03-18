package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardAccountEntity {
    @JsonProperty(value = "navn")
    private String name;

    @JsonProperty(value = "kortnummer")
    private String cardNumber;

    private String status;

    @JsonProperty(value = "kortId")
    private String cardId;

    @JsonProperty(value = "saldo")
    private Double balance;

    @JsonProperty(value = "disponibelt")
    private Double available;

    @JsonProperty(value = "kredittgrense")
    private Double creditLimit;

    @JsonProperty(value = "kortnummerGuid")
    private String cardNumberGuid;

    @JsonProperty(value = "transaksjonskontonummer")
    private String transactionAccountNumber;

    @JsonProperty(value = "transaksjonskontonummerGuid")
    private String transactionAccountNumberGuid;

    public CreditCardAccount toTinkCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(prepareCardModule())
                .withInferredAccountFlags()
                .withId(prepareIdModule())
                .setApiIdentifier(createApiIdentifier())
                .build();
    }

    private CreditCardModule prepareCardModule() {
        return CreditCardModule.builder()
                .withCardNumber(cardNumber)
                // spv uses positive amount for balance of a credit card
                .withBalance(ExactCurrencyAmount.inNOK(balance))
                .withAvailableCredit(ExactCurrencyAmount.inNOK(available))
                .withCardAlias(name)
                .build();
    }

    private IdModule prepareIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(transactionAccountNumber)
                .withAccountNumber(transactionAccountNumber)
                .withAccountName(name)
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.PAYMENT_CARD_NUMBER, cardNumber))
                .build();
    }

    private String createApiIdentifier() {
        return new ApiIdentifier(cardNumberGuid, cardId).getApiIdentifier();
    }
}
