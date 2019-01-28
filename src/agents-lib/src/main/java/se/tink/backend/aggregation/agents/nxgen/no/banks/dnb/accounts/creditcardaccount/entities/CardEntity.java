package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {
    private String kontonummer;
    private String korthavernavn;
    private String cardNumber;
    private String produktnavn;
    private String statustekst;
    private String bildeurl;
    private String cardid;
    private String corporateCard;
    private String activeStatus;

    public String getKontonummer() {
        return kontonummer;
    }

    public String getKorthavernavn() {
        return korthavernavn;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getProduktnavn() {
        return produktnavn;
    }

    public String getStatustekst() {
        return statustekst;
    }

    public String getBildeurl() {
        return bildeurl;
    }

    public String getCardid() {
        return cardid;
    }

    public String getCorporateCard() {
        return corporateCard;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

}
