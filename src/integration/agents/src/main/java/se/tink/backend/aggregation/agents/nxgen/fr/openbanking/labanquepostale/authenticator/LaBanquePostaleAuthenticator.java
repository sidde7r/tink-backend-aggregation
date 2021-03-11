package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator;

import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class LaBanquePostaleAuthenticator extends BerlinGroupAuthenticator
        implements OAuth2Authenticator {

    private final LaBanquePostaleApiClient apiClient;
    private final SessionStorage sessionStorage;

    public LaBanquePostaleAuthenticator(
            LaBanquePostaleApiClient apiClient, SessionStorage sessionStorage) {
        super(apiClient);
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return apiClient.getToken(code);
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        String errorType = callbackData.get(CallbackParams.ERROR);
        if (ErrorMessages.TIME_OUT.equals(errorType)) {
            throw ThirdPartyAppError.TIMED_OUT.exception();
        } else if (ErrorMessages.TEMP_UNAVAILABLE.equals(errorType)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }
}
