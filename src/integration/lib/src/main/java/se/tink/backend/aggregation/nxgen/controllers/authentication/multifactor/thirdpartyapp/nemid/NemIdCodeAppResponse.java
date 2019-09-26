package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

public class NemIdCodeAppResponse implements ThirdPartyAppResponse<String> {

    private final ThirdPartyAppStatus status;
    private final String reference;
    private final NemIdCodeAppPollResponse pollResponse;

    public NemIdCodeAppResponse(
            ThirdPartyAppStatus status, String reference, NemIdCodeAppPollResponse pollResponse) {
        this.status = status;
        this.reference = reference;
        this.pollResponse = pollResponse;
    }

    @Override
    public ThirdPartyAppStatus getStatus() {
        return status;
    }

    @Override
    public String getReference() {
        return reference;
    }

    public NemIdCodeAppPollResponse getPollResponse() {
        return pollResponse;
    }
}
