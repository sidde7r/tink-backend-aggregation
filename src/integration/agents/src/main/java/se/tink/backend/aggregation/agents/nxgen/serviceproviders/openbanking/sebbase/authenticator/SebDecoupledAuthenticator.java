package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Locale;
import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.HintCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.PollResponses;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.DecoupledAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.DecoupledAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.DecoupledTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.RefreshRequest;
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
    private final String locale;
    private String autoStartToken;
    private String authRequestId;
    private boolean isUserSign = false;

    public SebDecoupledAuthenticator(
            SebBaseApiClient apiClient,
            AgentConfiguration<SebConfiguration> agentConfiguration,
            String locale) {
        this.apiClient = apiClient;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.locale = locale;
    }

    @Override
    public String init(String ssn)
            throws BankServiceException, AuthorizationException, AuthenticationException {
        authRequestId =
                apiClient
                        .startDecoupledAuthorization(
                                DecoupledAuthRequest.builder()
                                        .clientId(configuration.getClientId())
                                        // Locale example: sv_SE
                                        .lang(
                                                Locale.forLanguageTag(locale.replace('_', '-'))
                                                        .getLanguage())
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
                throw BankIdError.AUTHORIZATION_REQUIRED.exception(
                        new LocalizableKey(ErrorMessages.REQUIRES_EXTRA_VERIFICATION));

            case PollResponses.FAILED:
                return (isUserSign ? BankIdStatus.CANCELLED : BankIdStatus.FAILED_UNKNOWN);

            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
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
        return Optional.ofNullable(
                apiClient
                        .getDecoupledToken(
                                DecoupledTokenRequest.builder()
                                        .authReqId(authRequestId)
                                        .clientId(configuration.getClientId())
                                        .clientSecret(configuration.getClientSecret())
                                        .redirectUri(redirectUrl)
                                        .build())
                        .toTinkToken());
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) throws SessionException {
        try {
            return refreshUsingDecoupled(refreshToken);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                return refreshUsingRedirect(refreshToken);
            }
            throw e;
        }
    }

    private Optional<OAuth2Token> refreshUsingDecoupled(String refreshToken) {
        return Optional.ofNullable(
                apiClient
                        .getDecoupledToken(
                                DecoupledTokenRequest.builder()
                                        .refreshToken(refreshToken)
                                        .clientId(configuration.getClientId())
                                        .clientSecret(configuration.getClientSecret())
                                        .redirectUri(redirectUrl)
                                        .build())
                        .toTinkToken());
    }

    private Optional<OAuth2Token> refreshUsingRedirect(String refreshToken) {
        try {
            RefreshRequest requestForm =
                    new RefreshRequest(
                            refreshToken,
                            configuration.getClientId(),
                            configuration.getClientSecret(),
                            QueryValues.REFRESH_TOKEN_GRANT);
            return Optional.ofNullable(apiClient.refreshToken(Urls.TOKEN, requestForm));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    || e.getResponse().getBody(ErrorResponse.class).isInvalidGrant()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }
}
