package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioAccountMapperEntity {
    @JsonProperty("ROW_ID")
    private Integer rowId;

    @JsonProperty("DEPA_ID")
    private String depotId;

    @JsonProperty("KONTO_NR")
    private String accountNumber;

    @JsonProperty("VALUTA_KOD")
    private String currency;

    @JsonProperty("KTO_FUNK_KOD")
    private Integer
            accountCode; // Looks like this is 3 for the mapping between depot id and the real
                         // account number;

    @JsonProperty("DISP_BEL")
    private Double availableAmount; // Looks like this value always is 0.00

    @JsonProperty("BOKF_SALDO")
    private Double bookedAmount; // Looks like this value always is 0.00

    @JsonProperty("KHAV")
    private String accountOwner; // Looks like this is just a bunch of white spaces

    public Integer getRowId() {
        return rowId;
    }

    public void setRowId(Integer rowId) {
        this.rowId = rowId;
    }

    public String getDepotId() {
        return depotId;
    }

    public void setDepotId(String depotId) {
        this.depotId = depotId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(Integer accountCode) {
        this.accountCode = accountCode;
    }

    public Double getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(Double availableAmount) {
        this.availableAmount = availableAmount;
    }

    public Double getBookedAmount() {
        return bookedAmount;
    }

    public void setBookedAmount(Double bookedAmount) {
        this.bookedAmount = bookedAmount;
    }

    public String getAccountOwner() {
        return accountOwner;
    }

    public void setAccountOwner(String accountOwner) {
        this.accountOwner = accountOwner;
    }
}
