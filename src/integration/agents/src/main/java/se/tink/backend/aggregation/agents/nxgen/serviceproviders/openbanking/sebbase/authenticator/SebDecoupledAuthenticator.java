package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.PollResponses;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.AuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration.SebConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class SebDecoupledAuthenticator implements BankIdAuthenticator<String> {

    private static final AggregationLogger log =
            new AggregationLogger(SebDecoupledAuthenticator.class);
    private final SebBaseApiClient apiClient;
    private final SebConfiguration configuration;
    private final String redirectUrl;
    private OAuth2Token oAuth2Token;
    private String autoStartToken;
    private String csrfToken;
    private String ssn;

    public SebDecoupledAuthenticator(
            SebBaseApiClient apiClient, AgentConfiguration<SebConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.configuration = agentConfiguration.getClientConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public String init(String ssn)
            throws BankServiceException, AuthorizationException, AuthenticationException {
        this.ssn = ssn;
        final AuthResponse response = apiClient.initBankId();
        autoStartToken = response.getAutoStartToken();
        csrfToken = response.getCsrfToken();

        return response.getAutoStartToken();
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        Preconditions.checkNotNull(Strings.emptyToNull(csrfToken), "Missing auto start token");

        final AuthResponse response = apiClient.collectBankId(csrfToken);
        csrfToken = response.getCsrfToken();

        switch (response.getStatus().toLowerCase()) {
            case PollResponses.COMPLETE:
                if (Strings.nullToEmpty(response.getHintCode())
                        .equalsIgnoreCase(PollResponses.UNKNOWN_BANK_ID)) {
                    return BankIdStatus.FAILED_UNKNOWN;
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
                        log.warn("Unhandled BankID hint: " + response.getHintCode());
                        return BankIdStatus.FAILED_UNKNOWN;
                }
            default:
                log.warn("Unhandled BankID status: " + response.getStatus());
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
