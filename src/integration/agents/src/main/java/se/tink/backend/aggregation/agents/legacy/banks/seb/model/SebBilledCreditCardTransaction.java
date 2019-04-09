package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// PCBW3243
@JsonIgnoreProperties(ignoreUnknown = true)
public class SebBilledCreditCardTransaction extends SebCreditCardTransaction {
    @JsonProperty("FAKTURA_NR")
    public Integer FAKTURA_NR;

    @JsonProperty("INVOICE_TYPE_CODE")
    public Integer INVOICE_TYPE_CODE;

    @JsonProperty("INVOICE_DATE")
    public String INVOICE_DATE;

    @JsonProperty("DUE_DATE")
    public String DUE_DATE;

    @JsonProperty("EXT_ACCOUNT_NO")
    public String EXT_ACCOUNT_NO;

    @JsonProperty("TOTAL_ADVICE_AMT")
    public Double TOTAL_ADVICE_AMT;

    @JsonProperty("ROUND_OF_AMT")
    public Double ROUND_OF_AMT;

    @JsonProperty("TOTAL_MIN_DUE_AMT")
    public Double TOTAL_MIN_DUE_AMT;

    @JsonProperty("IN_BAL_AMT")
    public Double IN_BAL_AMT;

    @JsonProperty("OUT_BAL_AMT")
    public Double OUT_BAL_AMT;

    @JsonProperty("TRANS_TYP")
    public String TRANS_TYP;

    @JsonProperty("ADVICE_ID")
    public Integer ADVICE_ID;
}
