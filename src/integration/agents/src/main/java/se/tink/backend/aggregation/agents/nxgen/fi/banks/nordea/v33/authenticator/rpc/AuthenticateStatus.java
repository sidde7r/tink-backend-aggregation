package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

@JsonObject
public class AuthenticateStatus implements ThirdPartyAppResponse<String> {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
        logger.info("tag={} {}", NordeaFIConstants.LogTags.NORDEA_FI_AUTHENTICATE, error);
        return ThirdPartyAppStatus.UNKNOWN;
    }
}
