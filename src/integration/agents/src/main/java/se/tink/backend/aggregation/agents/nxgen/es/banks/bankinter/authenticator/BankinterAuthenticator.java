package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page.AttemptsLimitExceededException;
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
    public void authenticate(String username, String password) {
        try {
            log.info("Submitting login form");
            authenticationClient.login(username, password);
            if (authenticationClient.isScaNeeded()) {
                log.info("Reached SCA form");
                authenticationClient.submitSca(supplementalInformationHelper);
                log.info("SCA form has been submitted successfully");
            }
            log.info("Login form has been submitted");
            authenticationClient.finishProcess();
            log.info("Authentication process has finished successfully");
        } catch (TimeoutException e) {
            throw ThirdPartyAppError.TIMED_OUT.exception(e);
        } catch (AttemptsLimitExceededException e) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(e);
        }
    }
}
