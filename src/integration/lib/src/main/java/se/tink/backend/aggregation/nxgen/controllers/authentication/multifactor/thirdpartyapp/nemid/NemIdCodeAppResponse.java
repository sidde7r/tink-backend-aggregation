package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

public class NemIdCodeAppResponse implements ThirdPartyAppResponse<String> {

    private final ThirdPartyAppStatus status;
    private final String reference;

    public NemIdCodeAppResponse(ThirdPartyAppStatus status, String reference) {
        this.status = status;
        this.reference = reference;
    }

    @Override
    public ThirdPartyAppStatus getStatus() {
        return status;
    }

    @Override
    public String getReference() {
        return reference;
    }
}
