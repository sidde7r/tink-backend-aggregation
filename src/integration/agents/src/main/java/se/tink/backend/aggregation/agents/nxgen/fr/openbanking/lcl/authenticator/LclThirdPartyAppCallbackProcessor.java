package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.authenticator;

import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.OAuth2ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.ThirdPartyAppCallbackProcessor;

public class LclThirdPartyAppCallbackProcessor extends ThirdPartyAppCallbackProcessor {

    public LclThirdPartyAppCallbackProcessor(
            OAuth2ThirdPartyAppRequestParamsProvider oAuth2ThirdPartyAppRequestParamsProvider) {
        super(oAuth2ThirdPartyAppRequestParamsProvider);
    }

    @Override
    public String processError(String error, Map<String, String> callbackData) {
        getCallbackElement(callbackData, OAuth2Constants.CallbackParams.ERROR_DESCRIPTION)
                .ifPresent(this::process);
        return super.processError(error, callbackData);
    }

    private void process(String errorDescription) {
        if (errorDescription.contains("PSU access denied")) {
            throw AuthorizationError.UNAUTHORIZED.exception();
        }
    }
}
