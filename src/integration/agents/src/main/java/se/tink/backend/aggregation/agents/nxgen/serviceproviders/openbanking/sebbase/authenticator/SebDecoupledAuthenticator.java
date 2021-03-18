package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
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
import se.tink.libraries.i18n.LocalizableKey;

public class SebDecoupledAuthenticator implements BankIdAuthenticator<String> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SebBaseApiClient apiClient;
    private final SebConfiguration configuration;
    private final String redirectUrl;
    private OAuth2Token oAuth2Token;
    private String autoStartToken;
    private String ssn;

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
        String authRequestId =
                apiClient
                        .startDecoupledAuthorization(
                                DecoupledAuthRequest.builder()
                                        .clientId(configuration.getClientId())
                                        .build())
                        .getAuthReqId();
        autoStartToken = apiClient.getDecoupledAuthStatus(authRequestId).getAutoStartToken();
        return authRequestId;
    }

    @Override
    public BankIdStatus collect(String authRequestId)
            throws AuthenticationException, AuthorizationException {
        DecoupledAuthResponse response = apiClient.getDecoupledAuthStatus(authRequestId);

        switch (response.getStatus().toLowerCase()) {
            case PollResponses.COMPLETE:
                if (Strings.nullToEmpty(response.getHintCode())
                        .equalsIgnoreCase(PollResponses.UNKNOWN_BANK_ID)) {
                    throw LoginError.NOT_SUPPORTED.exception(
                            new LocalizableKey(
                                    "Message from SEB - SEB needs you to verify your BankID before you can continue using the service. Visit www.seb.se or open the SEB app to verify your BankID. Note that you must be a customer of SEB to be able to use the service."));
                }

                startAuthorization();
                return BankIdStatus.DONE;
            case PollResponses.PENDING:
                return BankIdStatus.WAITING;
            case PollResponses.FAILED:
                switch (Strings.nullToEmpty(response.getHintCode()).toLowerCase()) {
                    case PollResponses.EXPIRED_TRANSACTION:
                    case PollResponses.START_FAILED:
                        return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
                    case PollResponses.CANCELLED:
                    case PollResponses.USER_CANCEL:
                        return BankIdStatus.CANCELLED;
                    case PollResponses.NO_CLIENT:
                        return BankIdStatus.NO_CLIENT;
                    default:
                        logger.warn("Unhandled BankID hint: " + response.getHintCode());
                        return BankIdStatus.FAILED_UNKNOWN;
                }
            default:
                logger.warn("Unhandled BankID status: " + response.getStatus());
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
            throws BankServiceException, AuthorizationException, AuthenticationException {
        return init(ssn);
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
