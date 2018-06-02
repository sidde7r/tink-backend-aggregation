package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class InitNewProfileResponse extends BaseResponse {

    private String snonce;
    private String challenge;

    public URL toCreateProfile() {
        return findLink(HandelsbankenConstants.URLS.Links.CREATE_PROFILE);
    }

    public URL toAuthenticate() {
        return findLink(HandelsbankenConstants.URLS.Links.AUTHENTICATE);
    }

    public String getSnonce() {
        return snonce;
    }

    public String getChallenge() {
        return challenge;
    }
}
