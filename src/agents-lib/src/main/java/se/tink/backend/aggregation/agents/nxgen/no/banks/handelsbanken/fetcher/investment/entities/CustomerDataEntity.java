package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerDataEntity {
    private String type;
    private String customerNumber;
    private String givenName;
    private String surName;
    private String email;
    private String phoneNumber;
    private List<AksjerAccountEntity> accounts;
    private boolean professional;

    public String getType() {
        return type;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurName() {
        return surName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public List<AksjerAccountEntity> getAccounts() {
        return accounts;
    }

    public boolean isProfessional() {
        return professional;
    }
}
