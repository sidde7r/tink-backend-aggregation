package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankEntity {
    private String name;
    private String url;
    private String bankId;
    private PrivateProfileEntity privateProfile;
    @JsonIgnore private String orgNumber;

    @JsonProperty("corporateProfiles")
    private List<BusinessProfileEntity> businessProfiles = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getBankId() {
        return bankId;
    }

    @JsonIgnore
    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }

    @JsonIgnore
    public PrivateProfileEntity getProfile() {
        if (businessProfiles.isEmpty()) {
            return privateProfile;
        }
        return businessProfiles.stream()
                .filter(
                        profile ->
                                profile.getCustomerNumber().contains(orgNumber)
                                        || profile.getCustomerNumber()
                                                .contains(orgNumber.replace("-", "")))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

    @JsonIgnore
    public String getHolderName() {
        // For business, activeProfileName is set to company name
        return Strings.isNullOrEmpty(getProfile().getActiveProfileName())
                ? getProfile().getCustomerName()
                : getProfile().getActiveProfileName();
    }
}
