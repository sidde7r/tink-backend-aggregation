package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AksjerAccountEntity {
    private String type;
    private String customerId;
    private String customerNumber;
    private boolean masterAccount;
    private String fullName;
    private String vpsAccountNo;
    private String bankAccountNo;
    private String customerGroup;
    private String instrumentSets;
    private String email;
    private boolean ask;
    private boolean marginAccount;
    private String status;

    public String getType() {
        return type;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public boolean isMasterAccount() {
        return masterAccount;
    }

    public String getFullName() {
        return fullName;
    }

    public String getVpsAccountNo() {
        return vpsAccountNo;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public String getCustomerGroup() {
        return customerGroup;
    }

    public String getInstrumentSets() {
        return instrumentSets;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAsk() {
        return ask;
    }

    public boolean isMarginAccount() {
        return marginAccount;
    }

    public String getStatus() {
        return status;
    }
}
