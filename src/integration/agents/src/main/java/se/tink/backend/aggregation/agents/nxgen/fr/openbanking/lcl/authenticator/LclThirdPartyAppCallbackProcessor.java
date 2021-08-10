package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.authenticator;

import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.ErrorType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.OAuth2ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.ThirdPartyAppCallbackProcessor;

public class LclThirdPartyAppCallbackProcessor extends ThirdPartyAppCallbackProcessor {

    public LclThirdPartyAppCallbackProcessor(
            OAuth2ThirdPartyAppRequestParamsProvider oAuth2ThirdPartyAppRequestParamsProvider) {
        super(oAuth2ThirdPartyAppRequestParamsProvider);
    }

    @Override
    public String processError(String error, Map<String, String> callbackData) {
        Optional.ofNullable(error).map(ErrorType::getErrorType).ifPresent(this::processErrorType);
        getCallbackElement(callbackData, OAuth2Constants.CallbackParams.ERROR_DESCRIPTION)
                .ifPresent(this::processErrorDescription);
        return super.processError(error, callbackData);
    }

    private void processErrorDescription(String errorDescription) {
        if (errorDescription.contains("PSU access denied")) {
            throw AuthorizationError.UNAUTHORIZED.exception();
        }
    }

    private void processErrorType(ErrorType errorType) {
        if (ErrorType.SERVER_ERROR.equals(errorType)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }
}
