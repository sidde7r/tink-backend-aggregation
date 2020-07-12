package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator;

import com.google.common.base.Strings;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public abstract class NordeaBaseAuthenticator implements OAuth2Authenticator {
    private static final Logger logger = LoggerFactory.getLogger(NordeaBaseAuthenticator.class);
    protected final NordeaBaseApiClient apiClient;

    public NordeaBaseAuthenticator(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public abstract URL buildAuthorizeUrl(String state);

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        GetTokenForm form =
                GetTokenForm.builder()
                        .setCode(code)
                        .setGrantType(NordeaBaseConstants.FormValues.AUTHORIZATION_CODE)
                        .setRedirectUri(apiClient.getRedirectUrl())
                        .build();

        return apiClient.getToken(form);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        OAuth2Token token = apiClient.refreshToken(refreshToken);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {

        final String code = callbackData.getOrDefault(OAuth2Constants.CallbackParams.CODE, null);
        final String httpMessage =
                callbackData.getOrDefault(
                        // this one is for running test locally
                        NordeaBaseConstants.CallbackParams.HTTP_MESSAGE,
                        // this one is for running in production
                        callbackData.getOrDefault(
                                NordeaBaseConstants.CallbackParams.HTTP_MESSAGE.toLowerCase(),
                                null));

        if (Strings.isNullOrEmpty(code)) {
            if (Strings.isNullOrEmpty(httpMessage)) {

                // no valid callbackData was found. Will log the callbackData keys
                logger.info(String.format("callbackData keys: %s", callbackData.keySet()));

                throw new IllegalStateException(
                        "callbackData did not contain 'code' or 'httpMessage'");
            }
            if (httpMessage.equalsIgnoreCase(NordeaBaseConstants.ErrorCodes.SESSION_CANCELLED)) {
                throw ThirdPartyAppError.CANCELLED.exception();
            }
            throw new IllegalStateException(
                    String.format("Unknown callbackData for 'httpMessage': %s", httpMessage));
        }
    }
}
