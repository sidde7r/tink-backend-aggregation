package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

@AllArgsConstructor
public class CbiThirdPartyAppAuthenticationStep implements AuthenticationStep {

    private final CbiThirdPartyAppRequestParamsProvider thirdPartyAppRequestParamsProvider;
    private final ConsentType consentType;
    private final ConsentManager consentManager;
    private final CbiUserState userState;
    private final StrongAuthenticationState strongAuthenticationState;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (request.getCallbackData() == null || request.getCallbackData().isEmpty()) {
            return AuthenticationStepResponse.requestForSupplementInformation(
                    new SupplementInformationRequester.Builder()
                            .withThirdPartyAppAuthenticationPayload(
                                    thirdPartyAppRequestParamsProvider.getPayload())
                            .withSupplementalWaitRequest(getWaitingConfiguration())
                            .build());
        }

        String codeValue =
                request.getCallbackData().getOrDefault(QueryKeys.CODE, consentType.getCode());

        if (!codeValue.equalsIgnoreCase(consentType.getCode())) {
            return AuthenticationStepResponse.requestForSupplementInformation(
                    new SupplementInformationRequester.Builder()
                            .withSupplementalWaitRequest(getWaitingConfiguration())
                            .build());
        }

        if (consentType.equals(ConsentType.BALANCE_TRANSACTION)) {
            processThirdPartyCallback();
            return AuthenticationStepResponse.authenticationSucceeded();
        }

        return AuthenticationStepResponse.executeNextStep();
    }

    private SupplementalWaitRequest getWaitingConfiguration() {
        return new SupplementalWaitRequest(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);
    }

    private void processThirdPartyCallback() throws AuthorizationException {
        try {
            if (consentManager.isConsentAccepted()) {
                userState.finishManualAuthenticationStep();
                consentManager.storeConsentValidUntilDateInCredentials();
            } else {
                throw new SessionException(SessionError.SESSION_EXPIRED);
            }
        } catch (SessionException e) {
            throw new AuthorizationException(
                    AuthorizationError.UNAUTHORIZED,
                    "Authorization failed, problem with consents.");
        }
    }

    @Override
    public String getIdentifier() {
        return getStepIdentifier(consentType);
    }

    public static String getStepIdentifier(ConsentType consentType) {
        return CbiThirdPartyAppAuthenticationStep.class.getSimpleName() + "_" + consentType;
    }
}
