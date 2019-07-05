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

    Optional<String> getAutostartToken();

    Optional<OAuth2Token> getAcessToken();

    Optional<OAuth2Token> refreshAccessToken(final String refreshToken);
}
