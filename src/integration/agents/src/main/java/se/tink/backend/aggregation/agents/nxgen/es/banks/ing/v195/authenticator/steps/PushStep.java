package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.ScaStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class PushStep extends AbstractAuthenticationStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushStep.class);
    public static final String STEP_ID = "IngPushStep";
    private final IngApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public PushStep(
            IngApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        super(STEP_ID);
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        supplementalInformationHelper.waitAndShowLoginDescription(null);

        LOGGER.info("Handling SCA by push notification.");

        // App polls up to 3 times without waiting, final status is 2, and has ticket
        // Endpoint responds with status 1 after 60s if the user hasn't answered
        for (int polls = 0; polls < 3; polls++) {
            LOGGER.info("Polling SCA status.");
            final ScaStatusResponse scaStatus = apiClient.getScaStatus(getProcessId(), true);

            if (!Strings.isNullOrEmpty(scaStatus.getTicket())) {
                LOGGER.info("SCA successful.");
                persistentStorage.put(Storage.CREDENTIALS_TOKEN, scaStatus.getRememberMeToken());
                apiClient.postLoginAuthResponse(scaStatus.getTicket());
                return AuthenticationStepResponse.authenticationSucceeded();
            }
        }

        LOGGER.info("SCA polling timed out.");
        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception("SCA polling timed out");
    }

    private String getProcessId() {
        return sessionStorage.get(Storage.LOGIN_PROCESS_ID);
    }
}
