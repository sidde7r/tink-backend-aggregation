package se.tink.backend.combined.integration;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.api.AuthenticationService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.utils.TestSSN;
import se.tink.backend.core.User;
import se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.rpc.FraudActivationRequest;
import se.tink.backend.rpc.auth.bankid.CollectBankIdAuthenticationResponse;
import se.tink.backend.rpc.auth.bankid.InitiateBankIdAuthenticationRequest;
import se.tink.backend.rpc.auth.bankid.InitiateBankIdAuthenticationResponse;
import se.tink.libraries.auth.AuthenticationMethod;

/**
 * TODO this is a unit test
 */
@Ignore
public class AuthenticationServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Test
    public void testNoUser() throws Exception {
        AuthenticationService authenticationService = serviceFactory.getAuthenticationService();

        InitiateBankIdAuthenticationRequest request = new InitiateBankIdAuthenticationRequest();
        request.setNationalId(TestSSN.FH);

        InitiateBankIdAuthenticationResponse initiateResponse = authenticationService.initiateBankIdAuthentication(request);

        CollectBankIdAuthenticationResponse collectResponse;

        do {
            collectResponse = authenticationService
                    .collectBankIdAuthentication(initiateResponse.getAuthenticationToken());

            log.info("Got " + collectResponse.getStatus());

            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        } while (collectResponse.getStatus() != BankIdAuthenticationStatus.NO_USER);
    }

    @Test
    public void testAuthenticate() throws Exception {
        User user = registerUser(randomUsername(), "testing", createUserProfile());

        FraudActivationRequest fr = new FraudActivationRequest();
        fr.setPersonIdentityNumber(TestSSN.FH);
        fr.setActivate(true);
        serviceFactory.getFraudService().activation(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), fr);
        waitForRefresh(user);

        AuthenticationService authenticationService = serviceFactory.getAuthenticationService();

        InitiateBankIdAuthenticationRequest request = new InitiateBankIdAuthenticationRequest();
        request.setNationalId(TestSSN.FH);

        InitiateBankIdAuthenticationResponse initiateResponse = authenticationService.initiateBankIdAuthentication(request);

        CollectBankIdAuthenticationResponse collectResponse;

        do {
            collectResponse = authenticationService
                    .collectBankIdAuthentication(initiateResponse.getAuthenticationToken());

            log.info("Got " + collectResponse.getStatus());

            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        } while (collectResponse.getStatus() != BankIdAuthenticationStatus.AUTHENTICATED);

        deleteUser(user);
    }
}
