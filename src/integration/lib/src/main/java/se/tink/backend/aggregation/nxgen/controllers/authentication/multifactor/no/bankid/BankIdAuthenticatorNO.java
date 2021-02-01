package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid;

import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;

public interface BankIdAuthenticatorNO {
    String init(String nationalId, String dob, String mobilenumber)
            throws AuthenticationException, AuthorizationException;

    BankIdStatus collect() throws AuthenticationException, AuthorizationException;

    default void finishActivation() throws SupplementalInfoException {
        // used in Sparebanken Sor, Handelsbanken, Sparebank1
    }
}
