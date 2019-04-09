package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// AKA PCBW4311 or PCBW4342
@JsonIgnoreProperties(ignoreUnknown = true)
public class SebTransaction {
    @JsonProperty("ROW_ID")
    public String ROW_ID;

    @JsonProperty("KONTO_NR")
    public String KONTO_NR;

    @JsonProperty("PCB_BOKF_DATUM")
    public String PCB_BOKF_DATUM;

    @JsonProperty("TRANSLOPNR")
    public Integer TRANSLOPNR;

    @JsonProperty("PCB_VALUTA_DATUM")
    public String PCB_VALUTA_DATUM;

    @JsonProperty("VERIF_NR")
    public String VERIF_NR;

    @JsonProperty("KK_TXT")
    public String KK_TXT;

    @JsonProperty("BOKN_REF")
    public String BOKN_REF;

    @JsonProperty("ROR_BEL")
    public String ROR_BEL;

    @JsonProperty("BOKF_SALDO")
    public String BOKF_SALDO;

    @JsonProperty("ROR_TYP")
    public String ROR_TYP;

    // May contain inköpsställe (Taxa 4x35: "ROR_X_INFO": "TAXA 4X35                DKK
    // 382,42-   KURS 1,1990")
    @JsonProperty("ROR_X_INFO")
    public String ROR_X_INFO;

    @JsonProperty("BGL_ROR")
    public String BGL_ROR;

    @JsonProperty("UPPDR_INFO_FL")
    public String UPPDR_INFO_FL;

    @JsonProperty("RTE_SATS1")
    public String RTE_SATS1;

    // Used in pending transactions (PCBW4342)
    @JsonProperty("DATUM")
    public String DATUM;

    @JsonProperty("BELOPP")
    public String BELOPP;

    @Override
    public String toString() {
        return KK_TXT + "-" + PCB_BOKF_DATUM + "-" + TRANSLOPNR;
    }
}
