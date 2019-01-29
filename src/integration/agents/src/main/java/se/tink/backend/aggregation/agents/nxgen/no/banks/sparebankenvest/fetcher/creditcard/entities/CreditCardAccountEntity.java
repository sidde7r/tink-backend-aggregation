package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CreditCardAccountEntity {
    @JsonProperty(value = "navn")
    private String name;
    @JsonProperty(value = "kortnummer")
    private String cardNumber;
    // kortnummerId is null - cannot define it!
    private String status;
    private String kid;
    private String kidGuid;
    @JsonProperty(value = "kortId")
    private String cardId;
    @JsonProperty(value = "saldo")
    private double balance;
    @JsonProperty(value = "disponibelt")
    private double available;
    @JsonProperty(value = "kredittgrense")
    private double creditLimit;
    private String sisteFakturadato;
    private double manedsbelop;
    private int produktnummer;
    private boolean isBedrift;
    private boolean isBedriftPersonAnsvar;
    // blokkertKode is null - cannot define it!
    private String blokkertdato;
    private String kortIdGuid;
    @JsonProperty(value = "kortnummerGuid")
    private String cardNumberGuid;
    private String korttypeGuid;
    private String nesteFakturadato;
    private String nesteInnbetalingDato;
    private double totaltForfall;
    private double forfall;
    private double reservertBelop;
    @JsonProperty(value = "transaksjonskontonummer")
    private String transactionAccountNumber;
    @JsonProperty(value = "transaksjonskontonummerGuid")
    private String transactionAccountNumberGuid;
    private String maskertKortnummer;
    private String maskertKredittkort;

    public CreditCardAccount toTinkCreditCardAccount() {
        // spv uses positive amount for balance of a credit card
        return CreditCardAccount.builder(transactionAccountNumber, Amount.inNOK(-balance), Amount.inNOK(available))
                .setAccountNumber(cardNumber)
                .setName(name)
                .setBankIdentifier(createBankIdentifier())
                .build();
    }

    private String createBankIdentifier() {
        return new BankIdentifier(cardNumberGuid, kidGuid).getBankIdentifier();
    }
}
