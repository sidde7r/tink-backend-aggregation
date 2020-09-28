package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class AuthorizeResponse extends BaseResponse {

    public URL toApplicationEntryPoint() {
        return findLink(HandelsbankenConstants.URLS.Links.APPLICATION_ENTRY_POINT);
    }

    public URL toApplicationExitPoint() {
        return findLink(HandelsbankenConstants.URLS.Links.APPLICATION_EXIT_POINT);
    }

    public URL toAuthorizeMandate() {
        return findLink(HandelsbankenConstants.URLS.Links.AUTHORIZE_MANDATE);
    }
}
