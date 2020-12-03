package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

@AllArgsConstructor
@Slf4j
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

        if (!codeValue.contains(consentType.getCode())) {
            return AuthenticationStepResponse.requestForSupplementInformation(
                    new SupplementInformationRequester.Builder()
                            .withSupplementalWaitRequest(getWaitingConfiguration())
                            .build());
        }

        processThirdPartyCallback(request.getCallbackData());

        if (consentType.equals(ConsentType.BALANCE_TRANSACTION)) {
            userState.finishManualAuthenticationStep();
            consentManager.storeConsentValidUntilDateInCredentials();
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

    private void processThirdPartyCallback(Map<String, String> callbackData)
            throws AuthorizationException {
        log.info("Received callback with query params data: " + callbackData);

        String authResult = callbackData.getOrDefault(QueryKeys.RESULT, QueryValues.SUCCESS);
        checkIfConsentRejected(authResult);
        try {
            consentManager.isConsentAccepted();
        } catch (SessionException e) {
            throw new AuthorizationException(
                    AuthorizationError.UNAUTHORIZED,
                    "Authorization failed, problem with consents.");
        }
    }

    private void checkIfConsentRejected(String authResult) {
        if (QueryValues.FAILURE.equals(authResult)) {
            throw ThirdPartyAppError.CANCELLED.exception();
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
