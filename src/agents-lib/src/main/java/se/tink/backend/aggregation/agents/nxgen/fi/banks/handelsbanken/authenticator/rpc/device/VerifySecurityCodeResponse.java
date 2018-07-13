package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class VerifySecurityCodeResponse extends BaseResponse {
    public URL toCreateProfile() {
        return findLink(HandelsbankenConstants.URLS.Links.CREATE_PROFILE);
    }
}
