package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@ToString
@Getter
@Slf4j
public class AuthenticateResponse implements ThirdPartyAppResponse<String> {
    private String sessionId;
    private String status;
    private String code;

    @Override
    public ThirdPartyAppStatus getStatus() {
        return NordeaFIConstants.AUTHENTICATION_STATUS_RESPONSE
                .translate(status)
                .orElseGet(this::logUnknownError);
    }

    @Override
    public String getReference() {
        return code;
    }

    public String getRawStatus() {
        return status;
    }

    private ThirdPartyAppStatus logUnknownError() {
        log.info("{} {}", NordeaFIConstants.LogTags.NORDEA_FI_AUTHENTICATE, status);
        return ThirdPartyAppStatus.UNKNOWN;
    }
}
