package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DataEntity {
    private boolean fundAccountCreateSuccess;
    private boolean portalProfileFetchSuccess;
    private boolean dnbCustomer;
    private int age;
    private boolean fundAccountCreated;
    private boolean employee;
    private boolean corporateUser;
    private String customerStatus;
    private long birthdate;
    private String insuranceCustomer;
    private long kundenummer;

    public boolean isFundAccountCreateSuccess() {
        return fundAccountCreateSuccess;
    }

    public boolean isPortalProfileFetchSuccess() {
        return portalProfileFetchSuccess;
    }

    public boolean isDnbCustomer() {
        return dnbCustomer;
    }

    public int getAge() {
        return age;
    }

    public boolean isFundAccountCreated() {
        return fundAccountCreated;
    }

    public boolean isEmployee() {
        return employee;
    }

    public boolean isCorporateUser() {
        return corporateUser;
    }

    public String getCustomerStatus() {
        return customerStatus;
    }

    public long getBirthdate() {
        return birthdate;
    }

    public String getInsuranceCustomer() {
        return insuranceCustomer;
    }

    public long getKundenummer() {
        return kundenummer;
    }
}
