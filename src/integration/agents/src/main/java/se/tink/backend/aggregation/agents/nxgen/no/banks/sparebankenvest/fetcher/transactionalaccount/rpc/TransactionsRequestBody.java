package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionsRequestBody {
    @JsonProperty("konto")
    private String accountNumber;

    private String fritekst;
    private String retning;
    private String kid;
    private int start;
    private String belopTil;
    private String periodeTil;
    private String periodeFra;
    private String belopFra;

    public void setKonto(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getKonto() {
        return accountNumber;
    }

    public void setFritekst(String fritekst) {
        this.fritekst = fritekst;
    }

    public String getFritekst() {
        return fritekst;
    }

    public void setRetning(String retning) {
        this.retning = retning;
    }

    public String getRetning() {
        return retning;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getKid() {
        return kid;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public void setBelopTil(String belopTil) {
        this.belopTil = belopTil;
    }

    public String getBelopTil() {
        return belopTil;
    }

    public void setPeriodeTil(String periodeTil) {
        this.periodeTil = periodeTil;
    }

    public String getPeriodeTil() {
        return periodeTil;
    }

    public void setPeriodeFra(String periodeFra) {
        this.periodeFra = periodeFra;
    }

    public String getPeriodeFra() {
        return periodeFra;
    }

    public void setBelopFra(String belopFra) {
        this.belopFra = belopFra;
    }

    public String getBelopFra() {
        return belopFra;
    }
}
