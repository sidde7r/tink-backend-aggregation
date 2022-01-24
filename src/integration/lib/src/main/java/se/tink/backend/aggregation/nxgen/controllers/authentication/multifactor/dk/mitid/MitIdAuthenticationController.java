package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.MIT_ID_LOG_TAG;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdFlowController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.libraries.credentials.service.UserAvailability;

@Slf4j
@RequiredArgsConstructor
public class MitIdAuthenticationController implements MultiFactorAuthenticator {

    private final UserAvailability userAvailability;
    private final WebDriverService driverService;
    private final AgentTemporaryStorage agentTemporaryStorage;

    private final MitIdAuthenticator authenticator;
    private final MitIdFlowController flowController;

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) {
        try {
            assertUserIsPresent();

            /*
            To make sure we will get all important HTTP traffic, proxy listeners should be registered
            before any authentication begins
             */
            flowController.registerProxyListeners();

            authenticator.initializeMitIdWindow(driverService);

            MitIdAuthenticationResult authenticationResult = flowController.authenticate();

            authenticator.finishAuthentication(authenticationResult);

        } catch (RuntimeException e) {
            log.error(
                    "{} MitID authentication error: {}\n{}",
                    MIT_ID_LOG_TAG,
                    e.getMessage(),
                    driverService.getFullPageSourceLog(3),
                    e);
            throw e;

        } finally {
            driverService.terminate(agentTemporaryStorage);
        }
    }

    private void assertUserIsPresent() {
        if (!userAvailability.isUserPresent()) {
            throw SessionError.SESSION_EXPIRED.exception(
                    "User is not present. Fail refresh before entering user's data into MitID");
        }
    }
}
