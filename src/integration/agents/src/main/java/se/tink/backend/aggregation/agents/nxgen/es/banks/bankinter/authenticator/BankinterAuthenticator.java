package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.ScaForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

@Slf4j
public class BankinterAuthenticator implements PasswordAuthenticator {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final BankinterAuthenticationClient authenticationClient;

    public BankinterAuthenticator(
            SupplementalInformationHelper supplementalInformationHelper,
            BankinterAuthenticationClient authenticationClient) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticationClient = authenticationClient;
    }

    @Override
    public void authenticate(String username, String password) throws LoginException {
        log.info("Submitting login form");
        String loginUrl = authenticationClient.login(username, password);
        authenticationClient.waitForErrorOrRedirect(LoginForm.SUBMIT_TIMEOUT_SECONDS, loginUrl);
        if (authenticationClient.isScaNeeded()) {
            log.info("Reached SCA form");
            String scaPage = authenticationClient.submitSca(supplementalInformationHelper);
            authenticationClient.waitForErrorOrRedirect(ScaForm.SUBMIT_TIMEOUT_SECONDS, scaPage);
            log.info("SCA form has been submitted successfully");
        }
        log.info("Login form has been submitted");
        authenticationClient.finishProcess();
        log.info("Authentication process has finished successfully");
    }
}
