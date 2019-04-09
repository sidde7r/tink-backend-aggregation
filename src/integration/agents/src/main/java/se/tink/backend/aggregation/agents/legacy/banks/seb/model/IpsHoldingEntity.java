package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IpsHoldingEntity {
    @JsonProperty("KONTO_NR")
    private String accountNumber;

    @JsonProperty("PLAC_TYP")
    private String type;

    @JsonProperty("VP_NAMN")
    private String name;

    @JsonProperty("VP_TYP")
    private String securityType;

    @JsonProperty("VP_FULLST_NAMN1")
    private String fullName;

    @JsonProperty("VALUTA_KOD_VP")
    private String currency;

    @JsonProperty("VP_KURS")
    private Double marketValue;

    @JsonProperty("BELOPP")
    private Double amount;

    @JsonProperty("ANDEL_ANT")
    private Double quantity;

    @JsonProperty("GAV")
    private Double averageAcqusitionPrice;

    @JsonProperty("TICKER_ID")
    private String tickerId;

    @JsonProperty("INB_TOT_BEL")
    private String totalAcquistionPrice;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Double marketValue) {
        this.marketValue = marketValue;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getAverageAcqusitionPrice() {
        return averageAcqusitionPrice;
    }

    public void setAverageAcqusitionPrice(Double averageAcqusitionPrice) {
        this.averageAcqusitionPrice = averageAcqusitionPrice;
    }

    public String getTickerId() {
        return tickerId;
    }

    public void setTickerId(String tickerId) {
        this.tickerId = tickerId;
    }

    public String getTotalAcquistionPrice() {
        return totalAcquistionPrice;
    }

    public void setTotalAcquistionPrice(String totalAcquistionPrice) {
        this.totalAcquistionPrice = totalAcquistionPrice;
    }
}
