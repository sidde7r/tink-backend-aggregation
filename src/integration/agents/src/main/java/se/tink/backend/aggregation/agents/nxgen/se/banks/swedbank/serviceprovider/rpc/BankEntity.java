package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BankEntity {
    private String name;
    private String url;
    private String bankId;
    private PrivateProfileEntity privateProfile;

    @JsonProperty("corporateProfiles")
    private List<BusinessProfileEntity> businessProfiles;

    @JsonIgnore
    public ProfileEntity getProfile(String profileId) {
        if (isPrivateProfileIdAndProfileIdSame(profileId)) {
            return privateProfile;
        }

        if (businessProfiles != null) {
            return getBankProfileEntityById(profileId);
        }

        throw new IllegalStateException("Profile not found");
    }

    private BusinessProfileEntity getBankProfileEntityById(String profileId) {
        return businessProfiles.stream()
                .filter(profile -> profile.getId().equalsIgnoreCase(profileId))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException("Could not find profile " + profileId));
    }

    @JsonIgnore
    private boolean isPrivateProfileIdAndProfileIdSame(String profileId) {
        return Objects.nonNull(privateProfile)
                && privateProfile.getId().equalsIgnoreCase(profileId);
    }

    @JsonIgnore
    public Optional<BusinessProfileEntity> getBusinessProfile(String organizationNumber) {
        return ListUtils.emptyIfNull(businessProfiles).stream()
                .filter(
                        profile ->
                                profile.getCustomerNumber()
                                        .replace("-", "")
                                        .contains(organizationNumber.replace("-", "")))
                .findFirst();
    }
}
