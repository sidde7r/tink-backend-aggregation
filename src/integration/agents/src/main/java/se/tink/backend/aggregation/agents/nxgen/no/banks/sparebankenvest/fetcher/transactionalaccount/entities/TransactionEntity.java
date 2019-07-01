package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private String tilKontonummer;
    private String valutadato;
    private String kid;
    private Object fraKontonavn;
    private String kategori;
    private boolean isBokfoert;

    @JsonProperty("beloep")
    private Beloep amount;

    private String detaljerId;
    private String mottakernavn;
    private String transaksjonType;
    private String melding;
    private String numeriskReferanse;
    private String fraKontonummer;
    private String id;
    private String arkivreferanse;

    @JsonProperty("bokfoeringsdato")
    private String accountingDate;

    @JsonProperty("beskrivelse")
    private String description;

    private String tilKontonavn;

    public void setTilKontonummer(String tilKontonummer) {
        this.tilKontonummer = tilKontonummer;
    }

    public String getTilKontonummer() {
        return tilKontonummer;
    }

    public void setValutadato(String valutadato) {
        this.valutadato = valutadato;
    }

    public String getValutadato() {
        return valutadato;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getKid() {
        return kid;
    }

    public void setFraKontonavn(Object fraKontonavn) {
        this.fraKontonavn = fraKontonavn;
    }

    public Object getFraKontonavn() {
        return fraKontonavn;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getKategori() {
        return kategori;
    }

    public void setIsBokfoert(boolean isBokfoert) {
        this.isBokfoert = isBokfoert;
    }

    public boolean isIsBokfoert() {
        return isBokfoert;
    }

    public void setBeloep(Beloep amount) {
        this.amount = amount;
    }

    public Beloep getBeloep() {
        return amount;
    }

    public void setDetaljerId(String detaljerId) {
        this.detaljerId = detaljerId;
    }

    public String getDetaljerId() {
        return detaljerId;
    }

    public void setMottakernavn(String mottakernavn) {
        this.mottakernavn = mottakernavn;
    }

    public String getMottakernavn() {
        return mottakernavn;
    }

    public void setTransaksjonType(String transaksjonType) {
        this.transaksjonType = transaksjonType;
    }

    public String getTransaksjonType() {
        return transaksjonType;
    }

    public void setMelding(String melding) {
        this.melding = melding;
    }

    public String getMelding() {
        return melding;
    }

    public void setNumeriskReferanse(String numeriskReferanse) {
        this.numeriskReferanse = numeriskReferanse;
    }

    public String getNumeriskReferanse() {
        return numeriskReferanse;
    }

    public void setFraKontonummer(String fraKontonummer) {
        this.fraKontonummer = fraKontonummer;
    }

    public String getFraKontonummer() {
        return fraKontonummer;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setArkivreferanse(String arkivreferanse) {
        this.arkivreferanse = arkivreferanse;
    }

    public String getArkivreferanse() {
        return arkivreferanse;
    }

    public void setaccountingDate(String accountingDate) {
        this.accountingDate = accountingDate;
    }

    public String getaccountingDate() {
        return accountingDate;
    }

    public void setBeskrivelse(String description) {
        this.description = description;
    }

    public String getBeskrivelse() {
        return description;
    }

    public void setTilKontonavn(String tilKontonavn) {
        this.tilKontonavn = tilKontonavn;
    }

    public String getTilKontonavn() {
        return tilKontonavn;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(description)
                .setAmount(ExactCurrencyAmount.of(amount.getVerdi(), "NOK"))
                .setDate(DateUtils.parseDate(accountingDate))
                .build();
    }
}
