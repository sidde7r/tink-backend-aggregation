package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper;

@RunWith(JUnitParamsRunner.class)
public class NemIdChoose2FAMethodStepTest {

    // can be any method - irrelevant
    private static final NemId2FAMethod SOME_2FA_METHOD_THAT_USER_WILL_PICK =
            NemId2FAMethod.CODE_APP;

    private NemIdDetect2FAMethodsStep detect2FAMethodsStep;
    private NemIdAskUserToChoose2FAStep askUserToChoose2FAMethodStep;
    private NemIdSwitchTo2FAScreenStep switchTo2FAScreenStep;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    private NemIdChoose2FAMethodStep choose2FAMethodStep;

    @Before
    public void setup() {
        detect2FAMethodsStep = mock(NemIdDetect2FAMethodsStep.class);
        askUserToChoose2FAMethodStep = mock(NemIdAskUserToChoose2FAStep.class);
        switchTo2FAScreenStep = mock(NemIdSwitchTo2FAScreenStep.class);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder =
                inOrder(detect2FAMethodsStep, askUserToChoose2FAMethodStep, switchTo2FAScreenStep);

        choose2FAMethodStep =
                new NemIdChoose2FAMethodStep(
                        detect2FAMethodsStep, askUserToChoose2FAMethodStep, switchTo2FAScreenStep);
    }

    @Test
    @Parameters(method = "allNemIdScreensTestParams")
    public void should_not_ask_user_to_choose_method_when_can_only_use_default_method(
            NemId2FAMethodScreen default2FAScreen) {
        // given
        NemIdDetect2FAMethodsResult detect2FAMethodsResult =
                NemIdDetect2FAMethodsResult.canOnlyUseDefaultMethod(default2FAScreen);
        when(detect2FAMethodsStep.detect2FAMethods(any())).thenReturn(detect2FAMethodsResult);

        // when
        NemId2FAMethod chosenMethod =
                choose2FAMethodStep.choose2FAMethod(credentials, default2FAScreen);

        // then
        assertThat(chosenMethod).isEqualTo(default2FAScreen.getSupportedMethod());

        mocksToVerifyInOrder.verify(detect2FAMethodsStep).detect2FAMethods(default2FAScreen);
        mocksToVerifyInOrder
                .verify(switchTo2FAScreenStep)
                .switchTo2FAMethodScreen(detect2FAMethodsResult, default2FAScreen);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "allPairwiseDifferent2FAScreensTestParams")
    public void should_ask_user_to_choose_method_when_can_toggle_between_methods(
            NemId2FAMethodScreen default2FAScreen, NemId2FAMethodScreen screenWeToggledTo) {
        // given
        NemIdDetect2FAMethodsResult detect2FAMethodsResult =
                NemIdDetect2FAMethodsResult.canToggleBetween2Methods(
                        default2FAScreen, screenWeToggledTo);
        when(detect2FAMethodsStep.detect2FAMethods(any())).thenReturn(detect2FAMethodsResult);

        when(askUserToChoose2FAMethodStep.askUserToChoose2FAMethod(any(), any()))
                .thenReturn(SOME_2FA_METHOD_THAT_USER_WILL_PICK);

        // when
        NemId2FAMethod chosenMethod =
                choose2FAMethodStep.choose2FAMethod(credentials, default2FAScreen);

        // then
        assertThat(chosenMethod).isEqualTo(SOME_2FA_METHOD_THAT_USER_WILL_PICK);

        mocksToVerifyInOrder.verify(detect2FAMethodsStep).detect2FAMethods(default2FAScreen);
        mocksToVerifyInOrder
                .verify(askUserToChoose2FAMethodStep)
                .askUserToChoose2FAMethod(
                        credentials,
                        ImmutableSet.of(
                                default2FAScreen.getSupportedMethod(),
                                screenWeToggledTo.getSupportedMethod()));
        mocksToVerifyInOrder
                .verify(switchTo2FAScreenStep)
                .switchTo2FAMethodScreen(
                        detect2FAMethodsResult,
                        NemId2FAMethodScreen.getScreenBy2FAMethod(
                                SOME_2FA_METHOD_THAT_USER_WILL_PICK));
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "allNemIdScreensTestParams")
    public void should_ask_user_to_choose_method_when_can_choose_method_from_popup(
            NemId2FAMethodScreen default2FAScreen) {
        // given
        NemIdDetect2FAMethodsResult detect2FAMethodsResult =
                NemIdDetect2FAMethodsResult.canChooseMethodFromPopup(
                        default2FAScreen, getAllMethodsAsSet());
        when(detect2FAMethodsStep.detect2FAMethods(any())).thenReturn(detect2FAMethodsResult);

        when(askUserToChoose2FAMethodStep.askUserToChoose2FAMethod(any(), any()))
                .thenReturn(SOME_2FA_METHOD_THAT_USER_WILL_PICK);

        // when
        NemId2FAMethod chosenMethod =
                choose2FAMethodStep.choose2FAMethod(credentials, default2FAScreen);

        // then
        assertThat(chosenMethod).isEqualTo(SOME_2FA_METHOD_THAT_USER_WILL_PICK);

        mocksToVerifyInOrder.verify(detect2FAMethodsStep).detect2FAMethods(default2FAScreen);
        mocksToVerifyInOrder
                .verify(askUserToChoose2FAMethodStep)
                .askUserToChoose2FAMethod(credentials, getAllMethodsAsSet());
        mocksToVerifyInOrder
                .verify(switchTo2FAScreenStep)
                .switchTo2FAMethodScreen(
                        detect2FAMethodsResult,
                        NemId2FAMethodScreen.getScreenBy2FAMethod(
                                SOME_2FA_METHOD_THAT_USER_WILL_PICK));
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] allNemIdScreensTestParams() {
        return NemId2FAMethodScreen.values();
    }

    @SuppressWarnings("unused")
    private Object[] allPairwiseDifferent2FAScreensTestParams() {
        return NemIdTestHelper.allNemId2FAPairwiseDifferentScreens().stream()
                .map(tuple -> new Object[] {tuple._1, tuple._2})
                .toArray(Object[]::new);
    }

    private Set<NemId2FAMethod> getAllMethodsAsSet() {
        return Stream.of(NemId2FAMethod.values()).collect(Collectors.toSet());
    }
}
