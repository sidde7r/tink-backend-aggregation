package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// PCBW3241
@JsonIgnoreProperties(ignoreUnknown = true)
public class SebCreditCardTransaction {
    @JsonProperty("ADV_AMT")
    public Integer ADV_AMT;

    @JsonProperty("ORIG_AMT_DATE")
    public String ORIG_AMT_DATE;
    
    @JsonProperty("ADV_AMT_CURR_CODE")
    public String ADV_AMT_CURR_CODE;

    @JsonProperty("AMT_DEC_QUANT")
    public Integer AMT_DEC_QUANT;

    @JsonProperty("CARD_NO")
    public String CARD_NO;

    @JsonProperty("CONV_RATE_QUANT")
    public Double CONV_RATE_QUANT;

    @JsonProperty("CURR_MARKUP_PERC")
    public Double CURR_MARKUP_PERC;

    @JsonProperty("ORIG_AMT")
    public Integer ORIG_AMT;

    @JsonProperty("ORIG_AMT_CURR_CODE")
    public String ORIG_AMT_CURR_CODE;

    @JsonProperty("ORIG_AMT_DEC_QUANT")
    public Integer ORIG_AMT_DEC_QUANT;

    @JsonProperty("POSTING_DATE")
    public String POSTING_DATE;

    @JsonProperty("ROW_ID")
    public String ROW_ID;

    @JsonProperty("SE_CITY_NAME")
    public String SE_CITY_NAME;

    @JsonProperty("SE_NAME")
    public String SE_NAME;

    @JsonProperty("TRANS_SUBTYPE_CODE")
    public Integer TRANS_SUBTYPE_CODE;

    @JsonProperty("TRANS_TYPE_CODE")
    public Integer TRANS_TYPE_CODE;

    @JsonProperty("VAT_AMT")
    public Integer VAT_AMT;
}