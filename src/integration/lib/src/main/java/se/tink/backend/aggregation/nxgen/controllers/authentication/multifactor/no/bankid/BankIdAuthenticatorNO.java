package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid;

import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface BankIdAuthenticatorNO {
    String init(String nationalId, String dob, String mobilenumber)
            throws AuthenticationException, AuthorizationException;

    BankIdStatus collect() throws AuthenticationException, AuthorizationException;
}
