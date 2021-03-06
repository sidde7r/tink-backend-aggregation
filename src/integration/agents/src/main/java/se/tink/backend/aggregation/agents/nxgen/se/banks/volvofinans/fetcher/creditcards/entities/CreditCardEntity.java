package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.rpc.CreditCardDataResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CreditCardEntity {
    @JsonProperty("kontoId")
    private String accountId; // used as api endpoint

    @JsonProperty("kontonummer")
    private String accountNumber;

    @JsonProperty("disponibeltBelopp")
    private double availableCredit;

    @JsonProperty("saldo")
    private double balance;

    @JsonProperty("namn")
    private String name;

    private String produkt;
    private double kreditgrans;
    private int antalKort;
    private boolean visaKoppling;
    private double poangSaldo;
    private double poangTillNastaCheck;
    private int procentUppfylldaTillNastaBonuscheck;
    private int antalKommandeBonuscheckar;
    private String kontoRoll;
    private boolean kreditavdelningsarende;
    private boolean avanmalt;
    private boolean kanAnvandasVidCarpaybetalning;
    private boolean kanOverfora;
    private boolean kanVisaKontoutdrag;
    private boolean kanHaEFaktura;
    private boolean kanBestallaExtrakort;
    private boolean kanAndraKreditgrans;
    private boolean kanKonverteraTillVisa;

    public CreditCardAccount toTinkAccount(CreditCardDataResponse creditData) {
        return CreditCardAccount.builder(
                        accountNumber,
                        ExactCurrencyAmount.inSEK(balance),
                        ExactCurrencyAmount.inSEK(availableCredit))
                .setAccountNumber(accountNumber)
                .setName(name)
                .setHolderName(getHolderName(creditData))
                .putInTemporaryStorage(VolvoFinansConstants.UrlParameters.ACCOUNT_ID, accountId)
                .build();
    }

    private HolderName getHolderName(CreditCardDataResponse creditData) {
        return new HolderName(
                creditData.stream()
                        .filter(c -> c.getAccountId().equals(accountId))
                        .findFirst()
                        .get()
                        .getCardName());
    }

    public String getAccountId() {
        return accountId;
    }
}
