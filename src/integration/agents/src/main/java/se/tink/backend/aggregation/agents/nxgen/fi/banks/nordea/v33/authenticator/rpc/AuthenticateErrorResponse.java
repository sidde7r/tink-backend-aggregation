package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

@JsonObject
@Slf4j
public class AuthenticateErrorResponse implements ThirdPartyAppResponse<String> {

    private String error;
    private String code;

    @Override
    public ThirdPartyAppStatus getStatus() {
        return NordeaFIConstants.AUTHENTICATION_RESPONSE
                .translate(error)
                .orElseGet(this::logUnknownError);
    }

    @Override
    public String getReference() {
        return code;
    }

    public String getRawError() {
        return error;
    }

    private ThirdPartyAppStatus logUnknownError() {
        log.info("{} {}", NordeaFIConstants.LogTags.NORDEA_FI_AUTHENTICATE, error);
        return ThirdPartyAppStatus.UNKNOWN;
    }
}
