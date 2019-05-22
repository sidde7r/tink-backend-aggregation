package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.libraries.identitydata.IdentityData;

public class UserAccountInfo {

    @JsonProperty("accountLocked")
    private boolean accountLocked;

    @JsonProperty("customerType")
    private String customerType;

    @JsonProperty("personalAdvisor")
    private PersonalAdvisor personalAdvisor;

    @JsonProperty("updateOrCreateKYCInfo")
    private boolean updateOrCreateKYCInfo;

    @JsonProperty("passwordUpdateRequired")
    private boolean passwordUpdateRequired;

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("customerServiceInfo")
    private CustomerServiceInfo customerServiceInfo;

    @JsonIgnore
    public IdentityData toIdentityData() {
        return IdentityData.builder().setFullName(customerName).setDateOfBirth(null).build();
    }
}
