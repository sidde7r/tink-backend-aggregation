package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class CreateProfileResponse extends BaseResponse {

    private String challenge;

    public URL toActivateProfile() {
        return findLink(HandelsbankenConstants.URLS.Links.ACTIVATE_PROFILE);
    }

    public String getChallenge() {
        return challenge;
    }
}
