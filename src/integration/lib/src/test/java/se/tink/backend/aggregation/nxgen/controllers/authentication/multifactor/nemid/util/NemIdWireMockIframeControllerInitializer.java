package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.USERNAME_INPUT;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdTokenValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdInitializeIframeStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdPerform2FAStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdVerifyLoginResponseStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdChoose2FAMethodStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.loginpage.NemIdLoginPageStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.libraries.i18n_aggregation.Catalog;

/** A happy path NemID iframe controller mock for wire mock testing. */
@RequiredArgsConstructor
public class NemIdWireMockIframeControllerInitializer implements NemIdIFrameControllerInitializer {

    private final String tokenToReturn;

    @Override
    public NemIdIFrameController initNemIdIframeController(
            NemIdParametersFetcher nemIdParametersFetcher,
            NemIdCredentialsProvider credentialsProvider,
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            MetricContext metricContext,
            AgentTemporaryStorage agentTemporaryStorage) {

        return new NemIdIFrameController(
                mock(NemIdWebDriverWrapper.class),
                agentTemporaryStorage,
                NemIdTestHelper.nemIdMetricsMock(),
                mock(NemIdTokenValidator.class),
                successfulIframeInitializationStep(nemIdParametersFetcher),
                nopLoginStep(),
                successfulLoginVerificationStep(),
                successfulChoose2FAMethodStep(),
                successfulPerform2FAStep(tokenToReturn));
    }

    private static NemIdInitializeIframeStep successfulIframeInitializationStep(
            NemIdParametersFetcher parametersFetcher) {
        NemIdWebDriverWrapper driverWrapper = mock(NemIdWebDriverWrapper.class);

        WebElement element = mock(WebElement.class);
        when(driverWrapper.waitForElement(IFRAME, 15)).thenReturn(Optional.of(element));
        when(driverWrapper.waitForElement(USERNAME_INPUT, 15)).thenReturn(Optional.of(element));

        return new NemIdInitializeIframeStep(
                driverWrapper,
                NemIdTestHelper.nemIdMetricsMock(),
                mock(NemIdCredentialsStatusUpdater.class),
                parametersFetcher);
    }

    private static NemIdLoginPageStep nopLoginStep() {
        return mock(NemIdLoginPageStep.class);
    }

    private static NemIdVerifyLoginResponseStep successfulLoginVerificationStep() {
        NemIdVerifyLoginResponseStep verifyLoginResponseStep =
                mock(NemIdVerifyLoginResponseStep.class);
        when(verifyLoginResponseStep.checkLoginResultAndGetDefault2FAScreen(any()))
                .thenReturn(NemId2FAMethodScreen.CODE_APP_SCREEN);
        return verifyLoginResponseStep;
    }

    private static NemIdChoose2FAMethodStep successfulChoose2FAMethodStep() {
        NemIdChoose2FAMethodStep choose2FAMethodStep = mock(NemIdChoose2FAMethodStep.class);
        when(choose2FAMethodStep.choose2FAMethod(any(), any())).thenReturn(NemId2FAMethod.CODE_APP);
        return choose2FAMethodStep;
    }

    private static NemIdPerform2FAStep successfulPerform2FAStep(String token) {
        NemIdPerform2FAStep perform2FAStep = mock(NemIdPerform2FAStep.class);
        when(perform2FAStep.authenticateToGetNemIdToken(any(), any())).thenReturn(token);
        return perform2FAStep;
    }
}
