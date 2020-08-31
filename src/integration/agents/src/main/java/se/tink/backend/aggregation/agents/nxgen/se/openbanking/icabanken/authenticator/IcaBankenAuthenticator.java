package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator;

import java.security.cert.CertificateException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
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
        persistentStorage.put(IcaBankenConstants.StorageKeys.TOKEN, response.getAccessToken());
        return response.toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

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
                ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);

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

        persistentStorage.put(IcaBankenConstants.StorageKeys.TOKEN, response.getAccessToken());
        return response.toOauthToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}
