package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

@JsonObject
public class AuthenticateStatus implements ThirdPartyAppResponse<String> {

    private String error;
    private String code;

    @Override
    public ThirdPartyAppStatus getStatus() {
        return NordeaFiConstants.AUTHENTICATION_RESPONSE.get(error);
    }

    @Override
    public String getReference() {
        return code;
    }
}
