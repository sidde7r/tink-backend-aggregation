package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;

public class DemobankMockNoBankIdAlwaysWaitingAuthenticator implements BankIdAuthenticatorNO {
    @Override
    public String init(String nationalId, String dob, String mobilenumber)
            throws AuthenticationException, AuthorizationException {
        return "dummy";
    }

    @Override
    public BankIdStatus collect() throws AuthenticationException, AuthorizationException {
        return BankIdStatus.WAITING;
    }
}
