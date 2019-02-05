package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class ActivateProfileResponse extends BaseResponse {

    private String profileId;

    public URL toCommitProfile() {
        return findLink(HandelsbankenConstants.URLS.Links.COMMIT_PROFILE);
    }

    public String getProfileId() {
        return profileId;
    }

    public boolean isInCreatePincodeFlow() {
        return searchLink(HandelsbankenConstants.URLS.Links.CREATE_PINCODE).isPresent();
    }
}
