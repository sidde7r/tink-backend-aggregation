package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdDetect2FAMethodsResult.ResultType.CAN_ONLY_USE_DEFAULT_METHOD;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdChoose2FAMethodStep {

    private final NemIdDetect2FAMethodsStep detect2FAMethodsStep;
    private final NemIdAskUserToChoose2FAStep askUserToChoose2FAMethodStep;
    private final NemIdSwitchTo2FAScreenStep switchTo2FAScreenStep;

    public NemId2FAMethod choose2FAMethod(
            Credentials credentials, NemId2FAMethodScreen default2FAScreen) {

        NemIdDetect2FAMethodsResult detect2FAMethodsResult =
                detect2FAMethodsStep.detect2FAMethods(default2FAScreen);

        NemId2FAMethod chosenMethod = choose2FAMethod(credentials, detect2FAMethodsResult);
        NemId2FAMethodScreen screenForChosenMethod =
                NemId2FAMethodScreen.getScreenBy2FAMethod(chosenMethod);

        switchTo2FAScreenStep.switchTo2FAMethodScreen(
                detect2FAMethodsResult, screenForChosenMethod);

        return chosenMethod;
    }

    private NemId2FAMethod choose2FAMethod(
            Credentials credentials, NemIdDetect2FAMethodsResult detect2FAMethodsResult) {

        if (detect2FAMethodsResult.getResultType() == CAN_ONLY_USE_DEFAULT_METHOD) {
            NemId2FAMethod supportedMethod =
                    detect2FAMethodsResult.getCurrentScreen().getSupportedMethod();
            log.info(
                    "{}[NemIdChoose2FAMethodStep] User can use only default method {}",
                    NEM_ID_PREFIX,
                    supportedMethod.getUserFriendlyName().get());
            return supportedMethod;
        }

        return askUserToChoose2FAMethodStep.askUserToChoose2FAMethod(
                credentials, detect2FAMethodsResult.getAvailableMethods());
    }
}
