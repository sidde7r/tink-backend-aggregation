package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.DecoupledAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaSeDecoupledAuthenticator implements BankIdAuthenticator<String> {

    private final NordeaSeApiClient apiClient;
    private String autoStartToken;
    private String ssn;
    private boolean isConfirmedPending = false;

    public NordeaSeDecoupledAuthenticator(NordeaSeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        this.ssn = ssn;
        DecoupledAuthenticationResponse authenticationResponse =
                apiClient.authenticateDecoupled(ssn);
        this.autoStartToken = authenticationResponse.getAutoStartToken();
        return authenticationResponse.getSessionId();
    }

    @Override
    public BankIdStatus collect(String sessionId)
            throws AuthenticationException, AuthorizationException {

        DecoupledAuthenticationResponse authenticationResponse;
        try {
            authenticationResponse = apiClient.getDecoupledAuthenticationStatus(sessionId);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST && isConfirmedPending) {
                return BankIdStatus.CANCELLED;
            } else if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
            }
            throw e;
        }

        switch (authenticationResponse.getStatus().toLowerCase()) {
            case Authentication.ASSIGNMENT_PENDING:
                return BankIdStatus.WAITING;
            case Authentication.CONFIRMATION_PENDING:
                isConfirmedPending = true;
                return BankIdStatus.WAITING;
            case Authentication.COMPLETED:
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
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) throws SessionException {
        return Optional.empty();
    }
}
