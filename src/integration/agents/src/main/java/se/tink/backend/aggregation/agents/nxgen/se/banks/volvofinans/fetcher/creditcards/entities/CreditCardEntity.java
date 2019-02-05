package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CreditCardEntity {
    @JsonProperty("kontoId")
    private String accountId;   // used as api endpoint
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

    public CreditCardAccount toTinkAccount() {
        return CreditCardAccount
                .builder(accountNumber, Amount.inSEK(balance), Amount.inSEK(availableCredit))
                .setAccountNumber(accountNumber)
                .setName(name)
                .putInTemporaryStorage(VolvoFinansConstants.UrlParameters.ACCOUNT_ID, accountId)
                .build();
    }
}
