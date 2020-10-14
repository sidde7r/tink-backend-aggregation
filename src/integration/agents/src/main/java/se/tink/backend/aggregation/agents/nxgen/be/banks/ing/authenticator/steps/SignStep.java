package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngComponents;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngProxyApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.GetEnrollmentsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.SignRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.SignResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngMiscUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngRequestFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

public class SignStep extends AbstractAuthenticationStep {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int ENROLLMENT_MAX_ATTEMPTS = 3;

    private final IngProxyApiClient ingProxyApiClient;
    private final IngStorage ingStorage;
    private final IngRequestFactory ingRequestFactory;

    public SignStep(IngComponents ingComponents) {
        super("SIGN");
        this.ingProxyApiClient = ingComponents.getIngProxyApiClient();
        this.ingStorage = ingComponents.getIngStorage();
        this.ingRequestFactory = ingComponents.getIngRequestFactory();
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        String challengeOtp = ingStorage.getForSession(Storage.OTP);

        sign(challengeOtp);

        getEnrollment();

        return AuthenticationStepResponse.executeNextStep();
    }

    private void sign(String responseCode) {
        SignRequestEntity request = ingRequestFactory.createSignRequest(responseCode);

        SignResponseEntity sign = ingProxyApiClient.sign(request);

        if (!"approved".equals(sign.getBasketStatus())) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception("Invalid sign response");
        }
    }

    private boolean getEnrollment() {
        String mobileAppId = ingStorage.getPermanent(Storage.MOBILE_APP_ID);

        for (int i = 0; i < ENROLLMENT_MAX_ATTEMPTS; i++) {
            GetEnrollmentsResponseEntity enrollment = ingProxyApiClient.getEnrollment(mobileAppId);
            if ("ACTIVATED".equals(enrollment.getStatus())) {
                return true;
            }
            LOGGER.warn("Retrying enrollment fetch due to wrong status, attempt {}", i + 1);
            IngMiscUtils.sleep(1000); // wait for enrollment to be ready
        }
        throw new IllegalStateException("Enrollment was not activated");
    }
}
