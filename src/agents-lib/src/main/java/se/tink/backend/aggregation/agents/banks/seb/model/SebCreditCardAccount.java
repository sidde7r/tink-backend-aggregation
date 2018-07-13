package se.tink.backend.aggregation.agents.banks.seb.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// PCBW3201
@JsonIgnoreProperties(ignoreUnknown = true)
public class SebCreditCardAccount {
    @JsonProperty("ROW_ID")
    public String ROW_ID;
    
    @JsonProperty("KORT_TYP")
    public String KORT_TYP;
    
    @JsonProperty("KONTO_ID")
    public Integer KONTO_ID;
    
    @JsonProperty("KONTO_AGARE")
    public String KONTO_AGARE;
    
    @JsonProperty("PRODUKT_NAMN")
    public String PRODUKT_NAMN;
    
    @JsonProperty("LIMIT_BELOPP")
    public Double LIMIT_BELOPP;
    
    @JsonProperty("SALDO_BELOPP")
    public Double SALDO_BELOPP;
    
    @JsonProperty("INVOICE_TYPE_CODE")
    public Integer INVOICE_TYPE_CODE;
    
    @JsonProperty("BILL_UNIT_HDL")
    public String BILL_UNIT_HDL;
    
    @JsonProperty("CARD_GROUP_HDL")
    public String CARD_GROUP_HDL;
    
    @JsonProperty("COLL_OF_DEBT_FL")
    public String COLL_OF_DEBT_FL;
    
    @JsonProperty("KORT_VISNINGS_TYP")
    public String KORT_VISNINGS_TYP;
}
