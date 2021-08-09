package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea.NordeaSeBusinessApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea.authenticator.rpc.DecoupledAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaSeBusinessDecoupledAuthenticator implements BankIdAuthenticator<String> {

    private final NordeaSeBusinessApiClient apiClient;
    private String autoStartToken;
    private String ssn;
    private boolean isConfirmedPending = false;
    private OAuth2Token accessToken;
    private String companyId;

    public NordeaSeBusinessDecoupledAuthenticator(
            NordeaSeBusinessApiClient apiClient, String companyId) {
        this.apiClient = apiClient;
        this.companyId = companyId;
    }

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        this.ssn = ssn;
        DecoupledAuthenticationResponse authenticationResponse =
                apiClient.authenticateDecoupled(ssn, setCompanyId(companyId, ssn));
        this.autoStartToken = authenticationResponse.getAutoStartToken();
        return authenticationResponse.getSessionId();
    }

    private String setCompanyId(String companyId, String ssn) {
        return companyId.equals("") ? ssn : companyId;
    }

    @Override
    public BankIdStatus collect(String sessionId)
            throws AuthenticationException, AuthorizationException {

        DecoupledAuthenticationResponse authenticationResponse;
        try {
            authenticationResponse = apiClient.getAuthenticationStatusDecoupled(sessionId);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                if (isConfirmedPending) {
                    return BankIdStatus.CANCELLED;
                } else {
                    return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
                }
            }
            throw e;
        }

        switch (authenticationResponse.getStatus().toLowerCase()) {
            case NordeaBaseConstants.BodyValuesSe.ASSIGNMENT_PENDING:
                return BankIdStatus.WAITING;
            case NordeaBaseConstants.BodyValuesSe.CONFIRMATION_PENDING:
                isConfirmedPending = true;
                return BankIdStatus.WAITING;
            case NordeaBaseConstants.BodyValuesSe.COMPLETED:
                String code =
                        apiClient
                                .authorizePsuAccountsDecoupled(authenticationResponse.getCode())
                                .getCode();
                accessToken = apiClient.getTokenDecoupled(code);
                return BankIdStatus.DONE;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    @Override
    public String refreshAutostartToken()
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        return init(ssn);
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) throws SessionException {
        return Optional.of(apiClient.refreshTokenDecoupled(refreshToken));
    }
}
