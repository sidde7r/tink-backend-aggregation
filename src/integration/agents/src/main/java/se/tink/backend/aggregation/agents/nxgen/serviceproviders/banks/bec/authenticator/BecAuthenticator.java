package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.Log.BEC_LOG_TAG;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ScaOptions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps.BecAuthWithCodeAppStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps.BecAuthWithKeyCardStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps.BecAutoAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps.BecChoose2FAMethodStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps.BecInitializeAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;

@Slf4j
@RequiredArgsConstructor
public class BecAuthenticator implements Authenticator {

    private final BecAutoAuthenticationStep autoAuthenticationStep;

    private final BecInitializeAuthenticationStep initializeAuthenticationStep;
    private final BecChoose2FAMethodStep choose2FAMethodStep;

    private final BecAuthWithCodeAppStep authWithCodeAppStep;
    private final BecAuthWithKeyCardStep authWithKeyCardStep;

    @Override
    public void authenticate(Credentials credentials) {
        if (autoAuthenticationStep.tryAutoAuthentication()) {
            return;
        }
        log.info("{} Switching to manual authentication", BEC_LOG_TAG);
        manualAuthentication();
    }

    private void manualAuthentication() {
        log.info("{} Fetching all 2FA options", BEC_LOG_TAG);
        List<String> all2FAOptions =
                initializeAuthenticationStep.initAuthenticationAndFetch2FAOptions();

        List<NemId2FAMethod> allNemId2FAMethods = getSupportedNemId2FAMethods(all2FAOptions);
        NemId2FAMethod chosen2FAMethod = choose2FAMethodStep.choose2FAMethod(allNemId2FAMethods);
        log.info("{} Chosen 2FA method: {}", BEC_LOG_TAG, chosen2FAMethod);

        switch (chosen2FAMethod) {
            case CODE_APP:
                authWithCodeAppStep.authenticate();
                break;
            case CODE_CARD:
                authWithKeyCardStep.authenticate();
                break;
            default:
                throw new IllegalStateException(
                        String.format(
                                "%s Unsupported 2FA method: %s", BEC_LOG_TAG, chosen2FAMethod));
        }
    }

    private List<NemId2FAMethod> getSupportedNemId2FAMethods(List<String> secondFactorOptions) {
        log.info("[BEC] Starting SCA methods validation");

        List<NemId2FAMethod> nemId2FAMethods =
                secondFactorOptions.stream()
                        .filter(
                                ScaOptions.SCA_OPTION_TO_SUPPORTED_NEM_ID_METHOD_MAPPING
                                        ::containsKey)
                        .map(ScaOptions.SCA_OPTION_TO_SUPPORTED_NEM_ID_METHOD_MAPPING::get)
                        .collect(Collectors.toList());

        boolean nemIdAvailable = !nemId2FAMethods.isEmpty();
        boolean mitIdAvailable = secondFactorOptions.contains(ScaOptions.MIT_ID_OPTION);
        boolean noAvailableMethods = secondFactorOptions.isEmpty();

        if (nemIdAvailable && mitIdAvailable) {
            log.info("[BEC] Both NemID and MitID available");
        } else if (nemIdAvailable) {
            log.info("[BEC] Only NemID available");
        } else if (mitIdAvailable) {
            log.info("[BEC] Only MitID available");
            throw LoginError.NO_AVAILABLE_SCA_METHODS.exception(
                    ErrorMessages.Authentication.MIT_ID_NOT_SUPPORTED_YET);
        } else if (noAvailableMethods) {
            log.info("[BEC] No available methods");
            throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
        } else {
            log.info("[BEC] Unknown available SCA methods: " + secondFactorOptions);
            throw NemIdError.SECOND_FACTOR_NOT_REGISTERED.exception();
        }

        return nemId2FAMethods;
    }
}
