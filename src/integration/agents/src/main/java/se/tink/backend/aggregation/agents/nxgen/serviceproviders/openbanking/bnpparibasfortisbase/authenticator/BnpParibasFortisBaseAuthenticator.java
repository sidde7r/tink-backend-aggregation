package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.authenticator;

import com.google.common.base.Strings;
import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.http.BnpParibasFortisBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BnpParibasFortisBaseAuthenticator implements OAuth2Authenticator {

    private final BnpParibasFortisBaseApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BnpParibasFortisBaseAuthenticator(
            BnpParibasFortisBaseApiClient apiClient, SessionStorage sessionStorage) {
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
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        final OAuth2Token accessToken = apiClient.refreshToken(refreshToken);
        sessionStorage.put(BnpParibasFortisBaseConstants.StorageKeys.OAUTH_TOKEN, accessToken);
        return accessToken;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(BnpParibasFortisBaseConstants.StorageKeys.OAUTH_TOKEN, accessToken);
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        String errorDescription = callbackData.getOrDefault(CallbackParams.ERROR_DESCRIPTION, "");
        if (!Strings.isNullOrEmpty(errorDescription)
                && errorDescription.contains(Errors.NO_ELIGIBLE_ACCOUNTS)) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(Errors.NO_ELIGIBLE_ACCOUNTS);
        }
    }
}
