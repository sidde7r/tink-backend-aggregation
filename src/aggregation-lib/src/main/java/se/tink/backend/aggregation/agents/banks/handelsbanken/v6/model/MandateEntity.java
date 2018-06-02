package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MandateEntity {
    private String customerName;
    private String customerNmbr;
    private String agreementNmbr;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerNmbr() {
        return customerNmbr;
    }

    public void setCustomerNmbr(String customerNmbr) {
        this.customerNmbr = customerNmbr;
    }

    public String getAgreementNmbr() {
        return agreementNmbr;
    }

    public void setAgreementNmbr(String agreementNmbr) {
        this.agreementNmbr = agreementNmbr;
    }
}
