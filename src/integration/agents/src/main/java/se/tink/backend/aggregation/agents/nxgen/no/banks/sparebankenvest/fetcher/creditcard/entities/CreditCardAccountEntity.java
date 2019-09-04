package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;

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

    @JsonProperty(value = "kontonummer")
    private String transactionAccountNumber;

    @JsonProperty(value = "kontonummerGuid")
    private String transactionAccountNumberGuid;

    public CreditCardAccount toTinkCreditCardAccount() {
        // spv uses positive amount for balance of a credit card
        return CreditCardAccount.builder(
                        transactionAccountNumber, Amount.inNOK(balance), Amount.inNOK(available))
                .setAccountNumber(cardNumber)
                .setName(name)
                .setBankIdentifier(createBankIdentifier())
                .build();
    }

    private String createBankIdentifier() {
        return new BankIdentifier(cardNumberGuid, cardId).getBankIdentifier();
    }
}
