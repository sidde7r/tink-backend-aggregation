package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.DecoupledAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class NordeaSeDecoupledAuthenticator implements BankIdAuthenticator {

    private final NordeaSeApiClient apiClient;
    private String autoStartToken;
    private String ssn;

    public NordeaSeDecoupledAuthenticator(NordeaSeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Object init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        this.ssn = ssn;
        DecoupledAuthenticationResponse authenticationResponse =
                apiClient.authenticateDecoupled(ssn);
        this.autoStartToken = authenticationResponse.getAutoStartToken();
        return null;
    }

    @Override
    public BankIdStatus collect(Object reference)
            throws AuthenticationException, AuthorizationException {
        return null;
    }

    @Override
    public Object refreshAutostartToken()
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
