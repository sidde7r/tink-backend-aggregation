package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidRecipientEntity extends AbstractResponse {

    private String bankName;
    private String clearingNo;
    private String accountNumber;
    private String accountNumberFormatted;
    private boolean isHandelsbanken;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getClearingNo() {
        return clearingNo;
    }

    public void setClearingNo(String clearingNo) {
        this.clearingNo = clearingNo;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumberFormatted() {
        return accountNumberFormatted;
    }

    public void setAccountNumberFormatted(String accountNumberFormatted) {
        this.accountNumberFormatted = accountNumberFormatted;
    }

    public boolean isHandelsbanken() {
        return isHandelsbanken;
    }

    public void setHandelsbanken(boolean handelsbanken) {
        isHandelsbanken = handelsbanken;
    }
}
