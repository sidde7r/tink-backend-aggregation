package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
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
        if (Strings.isNullOrEmpty(orgNumber)) {
            return privateProfile;
        }

        return getMatchingBusinessProfile().orElseThrow(IllegalStateException::new);
    }

    @JsonIgnore
    public Optional<BusinessProfileEntity> getMatchingBusinessProfile() {
        return businessProfiles.stream()
                .filter(
                        profile ->
                                profile.getCustomerNumber().contains(orgNumber)
                                        || profile.getCustomerNumber()
                                                .contains(orgNumber.replace("-", "")))
                .findAny();
    }

    @JsonIgnore
    public String getHolderName() {
        PrivateProfileEntity profile = getProfile();
        // For business, activeProfileName is set to company name
        return Strings.isNullOrEmpty(profile.getActiveProfileName())
                ? profile.getCustomerName()
                : profile.getActiveProfileName();
    }
}
