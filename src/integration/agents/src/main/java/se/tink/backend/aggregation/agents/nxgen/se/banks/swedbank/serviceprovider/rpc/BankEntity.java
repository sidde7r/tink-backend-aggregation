package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    public ProfileEntity getProfile(String profileId) {
        if (Objects.nonNull(privateProfile) && privateProfile.getId().equalsIgnoreCase(profileId)) {
            return privateProfile;
        }

        if (Objects.nonNull(businessProfiles)) {
            return businessProfiles.stream()
                    .filter(profile -> profile.getId().equalsIgnoreCase(profileId))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
        }

        throw new IllegalStateException("Profile not found");
    }

    @JsonIgnore
    public Optional<BusinessProfileEntity> getBusinessProfile(String organizationNumber) {
        if (Objects.isNull(businessProfiles)) {
            return Optional.empty();
        }

        return businessProfiles.stream()
                .filter(profile -> profile.getCustomerNumber().equalsIgnoreCase(organizationNumber))
                .findFirst();
    }
}
