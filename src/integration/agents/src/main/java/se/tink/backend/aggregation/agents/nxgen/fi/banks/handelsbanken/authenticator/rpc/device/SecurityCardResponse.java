package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SecurityCardResponse extends BaseResponse {

    private String securityKeyIndex;
    private String securityKeyCardId;

    public String getSecurityKeyIndex() {
        return securityKeyIndex;
    }

    public String getSecurityKeyCardId() {
        return securityKeyCardId;
    }

    public URL toVerifySecurityCode() {
        return findLink(HandelsbankenConstants.URLS.Links.VERIFY_SECURITY_CODE);
    }
}
