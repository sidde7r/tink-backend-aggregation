package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator;

import com.google.common.base.Strings;
import java.security.cert.CertificateException;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.AccountsErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.TokenErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class IcaBankenAuthenticator implements OAuth2Authenticator {
    private static final Logger logger = LoggerFactory.getLogger(IcaBankenAuthenticator.class);

    private final IcaBankenApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private String redirectUrl;
    private Credentials credentials;
    private String clientId;

    public IcaBankenAuthenticator(
            IcaBankenApiClient apiClient,
            PersistentStorage persistentStorage,
            AgentConfiguration<IcaBankenConfiguration> agentConfiguration,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.credentials = credentials;
        try {
            this.clientId =
                    CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());
        } catch (CertificateException e) {
            throw new IllegalStateException("Could not get organization identifier from QWAC", e);
        }
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        // Added check for Tink app credentials which were created when the provider didn't have
        // the SSN field. Put the credential in auth error so that users have to make an update
        // where they need to populate SSN. Not adding a fancy error message as we just want the
        // credentials in auth error and incorrect credentials is "good enough". This can be removed
        // when the Tink app is gone.
        if (Strings.isNullOrEmpty(credentials.getField(Field.Key.USERNAME))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        final Form params =
                Form.builder()
                        .put(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                        .put(QueryKeys.CLIENT_ID, clientId)
                        .put(QueryKeys.SCOPE, QueryValues.SCOPE)
                        .put(QueryKeys.REDIRECT_URI, redirectUrl)
                        .put(QueryKeys.SSN, credentials.getField(Field.Key.USERNAME))
                        .put(QueryKeys.STATE, state)
                        .build();

        return new URL(IcaBankenConstants.ProductionUrls.AUTH_PATH + "?" + params.toString());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        AuthorizationRequest request =
                AuthorizationRequest.builder()
                        .setClientId(clientId)
                        .setCode(code)
                        .setGrantType(IcaBankenConstants.QueryValues.AUTHORIZATION_CODE)
                        .setRedirectUri(redirectUrl)
                        .build();

        TokenResponse response = apiClient.exchangeAuthorizationCode(request);
        persistentStorage.put(IcaBankenConstants.StorageKeys.TOKEN, response.toOauthToken());

        verifyValidCustomerStatusOrThrow();

        return response.toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        logIfRefreshTokenWasUsedBefore(refreshToken);

        RefreshTokenRequest request =
                RefreshTokenRequest.builder()
                        .setClientId(clientId)
                        .setGrantType(IcaBankenConstants.QueryValues.REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .build();
        TokenResponse response;
        try {
            response = apiClient.exchangeRefreshToken(request);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                TokenErrorResponse errorResponse =
                        e.getResponse().getBody(TokenErrorResponse.class);

                if (errorResponse.isInternalServerError()) {
                    throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
                }

                // Temporarily logging the errors ICA throws at us to debug low success rates
                logger.info(
                        "Error when refreshing access token: error: {}, description: {}",
                        errorResponse.getError(),
                        errorResponse.getErrorDescription());
                throw e;
            }

            throw e;
        }

        persistentStorage.put(IcaBankenConstants.StorageKeys.TOKEN, response.toOauthToken());

        verifyValidCustomerStatusOrThrow();

        return response.toOauthToken();
    }

    private void logIfRefreshTokenWasUsedBefore(String refreshToken) {
        String lastUsedRefreshToken = persistentStorage.get(StorageKeys.LAST_USED_REFRESH_TOKEN);
        if (refreshToken.equals(lastUsedRefreshToken)) {
            logger.info("Using a refresh token that has already been consumed.");
        }
        persistentStorage.put(StorageKeys.LAST_USED_REFRESH_TOKEN, refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        String errorType = callbackData.getOrDefault(CallbackParams.ERROR, null);

        if (!IcaBankenConstants.ErrorTypes.UNKNOWN.equalsIgnoreCase(errorType)) {
            return;
        }

        String errorDescription =
                callbackData.getOrDefault(CallbackParams.ERROR_DESCRIPTION, "").toLowerCase();

        // ICA returns "userCancel", "cancelled", or "User Cancelled" if user cancels the SCA
        if (errorDescription.contains(IcaBankenConstants.ErrorMessages.CANCEL)) {
            throw ThirdPartyAppError.CANCELLED.exception();
        }

        // ICA returns "startFailed" if user waits for more than 30 seconds to sign with bankID,
        // treating it as a timeout.
        if (errorDescription.contains(IcaBankenConstants.ErrorMessages.START_FAILED)) {
            throw ThirdPartyAppError.TIMED_OUT.exception();
        }

        if (errorDescription.contains(
                        IcaBankenConstants.ErrorMessages.UNEXPECTED_INTERNAL_EXCEPTION)
                || errorDescription.contains(
                        IcaBankenConstants.ErrorMessages.INTERNAL_SERVER_ERROR)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    /**
     * Verifies that customer has updated KYC info and is a customer by trying to fetch accounts.
     *
     * @throws LoginException if no accounts can be found for the user
     * @throws AuthorizationException if KYC info needs to be updated at bank
     */
    private void verifyValidCustomerStatusOrThrow() throws LoginException, AuthorizationException {
        try {
            apiClient.fetchAccounts();
        } catch (HttpResponseException e) {
            int responseStatus = e.getResponse().getStatus();

            if (!(responseStatus == HttpStatus.SC_FORBIDDEN
                    || responseStatus == HttpStatus.SC_NOT_FOUND)) {
                throw e;
            }

            AccountsErrorResponse errorResponse =
                    e.getResponse().getBody(AccountsErrorResponse.class);

            if (errorResponse.isOldKycInfoError()) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        IcaBankenConstants.EndUserMessage.MUST_ANSWER_KYC.getKey(), e);
            }

            if (errorResponse.isNoAccountInformation()) {
                throw LoginError.NOT_CUSTOMER.exception(e);
            }

            throw e;
        }
    }
}
