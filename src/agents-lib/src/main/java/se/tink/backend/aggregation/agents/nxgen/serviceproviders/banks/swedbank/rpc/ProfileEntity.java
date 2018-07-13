package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class ProfileEntity {
    private String activeProfileLanguage;
    private String url;
    private String bankId;
    private String customerNumber;
    private String bankName;
    private boolean customerInternational;
    private String customerName;
    private boolean youthProfile;

    public String getActiveProfileLanguage() {
        return activeProfileLanguage;
    }

    public String getUrl() {
        return url;
    }

    public String getBankId() {
        return bankId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public boolean isCustomerInternational() {
        return customerInternational;
    }

    public String getCustomerName() {
        return customerName;
    }

    public boolean isYouthProfile() {
        return youthProfile;
    }
}
