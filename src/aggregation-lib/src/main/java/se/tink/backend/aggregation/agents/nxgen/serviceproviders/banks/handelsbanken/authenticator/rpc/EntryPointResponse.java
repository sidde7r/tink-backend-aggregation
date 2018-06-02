package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class EntryPointResponse extends BaseResponse {

    public URL toPinnedActivation() {
        return findLink(HandelsbankenConstants.URLS.Links.PINNED_ACTIVATION);
    }

    public URL toPinnedLogin() {
        return findLink(HandelsbankenConstants.URLS.Links.PINNED_LOGIN);
    }

    public URL toBankIdLogin() {
        return findLink(HandelsbankenConstants.URLS.Links.BANKID_LOGIN);
    }
}
