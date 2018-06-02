package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandshakeResponse extends BaseResponse {

    private String serverHello;

    public URL toGetServerProfile() {
        return findLink(HandelsbankenConstants.URLS.Links.GET_SERVER_PROFILE);
    }

    public String getServerHello() {
        return serverHello;
    }
}
