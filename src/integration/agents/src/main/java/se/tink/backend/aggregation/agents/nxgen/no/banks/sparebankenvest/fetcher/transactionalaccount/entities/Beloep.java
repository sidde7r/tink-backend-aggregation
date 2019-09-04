package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Beloep {
    private BigDecimal verdi;
    private BigDecimal verdiNok;
    private String valutakode;

    public void setVerdi(BigDecimal verdi) {
        this.verdi = verdi;
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    public void setVerdiNok(BigDecimal verdiNok) {
        this.verdiNok = verdiNok;
    }

    public BigDecimal getVerdiNok() {
        return verdiNok;
    }

    public void setValutakode(String valutakode) {
        this.valutakode = valutakode;
    }

    public String getValutakode() {
        return valutakode;
    }
}
