package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

@JsonObject
public class AuthenticateStatus implements ThirdPartyAppResponse<String> {
    private static final AggregationLogger log = new AggregationLogger(AuthenticateStatus.class);
    private String error;
    private String code;

    @Override
    public ThirdPartyAppStatus getStatus() {
        return NordeaFIConstants.AUTHENTICATION_RESPONSE.translate(error).orElse(logUnknownError());
    }

    @Override
    public String getReference() {
        return code;
    }

    private ThirdPartyAppStatus logUnknownError() {
        log.infoExtraLong(error, NordeaFIConstants.LogTags.NORDEA_FI_AUTHENTICATE);
        return ThirdPartyAppStatus.UNKNOWN;
    }
}
