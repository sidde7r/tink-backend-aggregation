package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.HintCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.PollResponses;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.DecoupledAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.DecoupledAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration.SebConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.i18n.LocalizableKey;

public class SebDecoupledAuthenticator implements BankIdAuthenticator<String> {

    private final SebBaseApiClient apiClient;
    private final SebConfiguration configuration;
    private final String redirectUrl;
    private OAuth2Token oAuth2Token;
    private String autoStartToken;
    private String ssn;
    private String authRequestId;
    private boolean isUserSign = false;

    public SebDecoupledAuthenticator(
            SebBaseApiClient apiClient, AgentConfiguration<SebConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public String init(String ssn)
            throws BankServiceException, AuthorizationException, AuthenticationException {
        this.ssn = ssn;
        authRequestId =
                apiClient
                        .startDecoupledAuthorization(
                                DecoupledAuthRequest.builder()
                                        .clientId(configuration.getClientId())
                                        .build())
                        .getAuthReqId();
        autoStartToken = apiClient.getDecoupledAuthStatus(authRequestId).getAutostartToken();
        return authRequestId;
    }

    @Override
    public BankIdStatus collect(String authRequestId)
            throws AuthenticationException, AuthorizationException {
        DecoupledAuthResponse response;
        try {
            response = apiClient.getDecoupledAuthStatus(authRequestId);
        } catch (HttpResponseException e) {
            // SEB ends up with HTTP 500 after about 3 minutes for both cases of
            // isUserSign=true and isUserSign=false
            if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                return BankIdStatus.TIMEOUT;
            }
            throw e;
        }
        String hintCode = Strings.nullToEmpty(response.getHintCode());

        switch (response.getStatus().toLowerCase()) {
            case PollResponses.COMPLETE:
                //                startAuthorization();
                return BankIdStatus.DONE;

            case PollResponses.PENDING:
                // SEB renews AST every 20 seconds (10 seconds before expiry)
                if (!isUserSign && hintCode.equalsIgnoreCase(HintCodes.USER_SIGN)) {
                    isUserSign = true;
                } else if (hintCode.equalsIgnoreCase(HintCodes.OUTSTANDING_TRANSACTION)
                        && !autoStartToken.equals(response.getAutostartToken())) {
                    autoStartToken = response.getAutostartToken();
                    return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
                }
                return BankIdStatus.WAITING;

            case PollResponses.REQUIRES_EXTRA_VERIFICATION:
                throw LoginError.NOT_SUPPORTED.exception(
                        new LocalizableKey(ErrorMessages.REQUIRES_EXTRA_VERIFICATION));

            case PollResponses.FAILED:
                if (isUserSign) {
                    return BankIdStatus.CANCELLED;
                }

            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private void startAuthorization() {
        AuthorizeResponse response =
                apiClient.getAuthorization(configuration.getClientId(), redirectUrl);
        final String consentFormVerifier = response.getConsentFormVerifier();
        String code = response.getCode();
        if (!Strings.isNullOrEmpty(consentFormVerifier)) {
            final String requestForm = new AuthorizeRequest(consentFormVerifier).toData();
            AuthorizeResponse authorizeResponse = apiClient.postAuthorization(requestForm);
            code = authorizeResponse.getCode();
        }
        TokenRequest request =
                new TokenRequest(
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        redirectUrl,
                        QueryValues.AUTH_CODE_GRANT,
                        code,
                        QueryValues.SCOPE);
        oAuth2Token = apiClient.getToken(request);
    }

    @Override
    public String refreshAutostartToken()
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        return authRequestId;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.ofNullable(oAuth2Token);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) throws SessionException {
        RefreshRequest requestForm =
                new RefreshRequest(
                        refreshToken,
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        QueryValues.REFRESH_TOKEN_GRANT);
        return Optional.ofNullable(apiClient.refreshToken(Urls.TOKEN, requestForm));
    }
}
