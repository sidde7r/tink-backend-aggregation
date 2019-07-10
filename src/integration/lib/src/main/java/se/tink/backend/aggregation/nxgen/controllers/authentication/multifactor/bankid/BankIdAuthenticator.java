package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid;

import java.util.Optional;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public interface BankIdAuthenticator<T> {
    T init(String ssn) throws BankIdException, BankServiceException, AuthorizationException;

    BankIdStatus collect(T reference) throws AuthenticationException, AuthorizationException;

    /**
     * For bankid authenticators built for PSD2 agents this should return the OAuth2Token retrieved
     * in its authentication calls. If it is an authenticator that does not use OAuth2, this should
     * return Optional.empty().
     *
     * <p>Basically, if it is a PSD2 agent with bankid, implement this method to return the
     * OAuth2Token if it is an RE agent, return Optional.empty()
     *
     * @return an Optional containing the OAuth2Token or Optional.empty() if there is no token.
     */
    Optional<String> getAutostartToken();

    Optional<OAuth2Token> getAcessToken();

    /**
     * Attempts to use the refresh token supplied to return a new OAuth2Token to extend validity.
     *
     * @param refreshToken
     * @return An Optional containing the OAuth2Token or Optional.empty()
     */
    Optional<OAuth2Token> refreshAccessToken(final String refreshToken);
}
