package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustodyAccountEntity extends AbstractResponse {
    private String custodyAccountNumber;
    private String iskAccountNumber;
    private String accountNumberFormatted;
    private String type;
    private String title;
    private String infoText;
    private AmountEntity marketValue;
    private String pensionSystemStr;

    public String getCustodyAccountNumber() {
        return custodyAccountNumber;
    }

    public void setCustodyAccountNumber(String custodyAccountNumber) {
        this.custodyAccountNumber = custodyAccountNumber;
    }

    public String getIskAccountNumber() {
        return iskAccountNumber;
    }

    public void setIskAccountNumber(String iskAccountNumber) {
        this.iskAccountNumber = iskAccountNumber;
    }

    public String getAccountNumberFormatted() {
        return accountNumberFormatted;
    }

    public void setAccountNumberFormatted(String accountNumberFormatted) {
        this.accountNumberFormatted = accountNumberFormatted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(AmountEntity marketValue) {
        this.marketValue = marketValue;
    }

    public String getPensionSystemStr() {
        return pensionSystemStr;
    }

    public void setPensionSystemStr(String pensionSystemStr) {
        this.pensionSystemStr = pensionSystemStr;
    }
}
