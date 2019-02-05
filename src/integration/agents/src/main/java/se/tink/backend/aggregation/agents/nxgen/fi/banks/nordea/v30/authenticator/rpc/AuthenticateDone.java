package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

public class AuthenticateDone implements ThirdPartyAppResponse<String> {

    @Override
    public ThirdPartyAppStatus getStatus() {
        return ThirdPartyAppStatus.DONE;
    }

    @Override
    public String getReference() {
        return null;
    }
}
