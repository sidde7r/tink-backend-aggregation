package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class NemIdAskUserToChoose2FAStepTest {

    private Catalog catalog;
    private NemIdCredentialsStatusUpdater statusUpdater;
    private SupplementalInformationController supplementalInformationController;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    private NemIdAskUserToChoose2FAStep askUserToChoose2FAStep;

    @Before
    public void setup() {
        catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("something not empty");
        statusUpdater = mock(NemIdCredentialsStatusUpdater.class);
        supplementalInformationController = mock(SupplementalInformationController.class);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder =
                inOrder(credentials, statusUpdater, supplementalInformationController);

        askUserToChoose2FAStep =
                new NemIdAskUserToChoose2FAStep(
                        nemIdMetricsMock(),
                        statusUpdater,
                        catalog,
                        supplementalInformationController);
    }

    @Test
    @Parameters(method = "askUserTestParams")
    public void should_ask_user_to_choose_from_all_available_methods(AskUserTestParams testParams) {
        // given
        Set<NemId2FAMethod> availableMethods = testParams.getAvailableMethods();
        String chosenSupplementalFieldKey = testParams.getChosenSupplementalFieldKey();
        NemId2FAMethod expectedChosen2FAMethod = testParams.getExpectedChosen2FAMethod();

        // and
        mockSupplementalInfoControllerResponse(
                singletonMap(NemIdChoose2FAMethodField.FIELD_KEY, chosenSupplementalFieldKey));

        // when
        NemId2FAMethod chosenMethod =
                askUserToChoose2FAStep.askUserToChoose2FAMethod(credentials, availableMethods);

        // then
        assertThat(chosenMethod).isEqualTo(expectedChosen2FAMethod);

        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.CHOOSE_NEM_ID_METHOD);

        ArgumentCaptor<Field> argumentCaptor = ArgumentCaptor.forClass(Field.class);
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues().size()).isEqualTo(1);

        Field field = argumentCaptor.getAllValues().get(0);
        assertThat(field).isEqualTo(NemIdChoose2FAMethodField.build(catalog, availableMethods));

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] askUserTestParams() {
        return new Object[] {
            AskUserTestParams.builder()
                    .availableMethods(singleton(NemId2FAMethod.CODE_APP))
                    .chosenSupplementalFieldKey(NemId2FAMethod.CODE_APP.getSupplementalInfoKey())
                    .expectedChosen2FAMethod(NemId2FAMethod.CODE_APP)
                    .build(),
            AskUserTestParams.builder()
                    .availableMethods(
                            ImmutableSet.of(NemId2FAMethod.CODE_APP, NemId2FAMethod.CODE_CARD))
                    .chosenSupplementalFieldKey(NemId2FAMethod.CODE_CARD.getSupplementalInfoKey())
                    .expectedChosen2FAMethod(NemId2FAMethod.CODE_CARD)
                    .build(),
            AskUserTestParams.builder()
                    .availableMethods(
                            ImmutableSet.of(
                                    NemId2FAMethod.CODE_APP,
                                    NemId2FAMethod.CODE_CARD,
                                    NemId2FAMethod.CODE_TOKEN))
                    .chosenSupplementalFieldKey(NemId2FAMethod.CODE_TOKEN.getSupplementalInfoKey())
                    .expectedChosen2FAMethod(NemId2FAMethod.CODE_TOKEN)
                    .build()
        };
    }

    private void mockSupplementalInfoControllerResponse(Map<String, String> response) {
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(response);
    }

    @Data
    @Builder
    private static class AskUserTestParams {
        private final Set<NemId2FAMethod> availableMethods;
        private final String chosenSupplementalFieldKey;
        private final NemId2FAMethod expectedChosen2FAMethod;
    }
}
