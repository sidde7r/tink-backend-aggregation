package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class ChallengeResponse extends BaseResponse {

    private String challenge;

    public String getChallenge() {
        return challenge;
    }

    public URL toValidateSignature() {
        return findLink(HandelsbankenConstants.URLS.Links.VALIDATE_SIGNATURE);
    }
}
