package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.USERNAME_INPUT;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.loginpage.NemIdLoginPageStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.loginpage.NemIdPasswordField;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.loginpage.NemIdUserIdField;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class NemIdLoginPageStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdCredentialsStatusUpdater statusUpdater;
    private Credentials credentials;
    private NemIdCredentialsProvider credentialsProvider;
    private Catalog catalog;
    private SupplementalInformationController supplementalInformationController;

    private NemIdLoginPageStep loginPageStep;

    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        statusUpdater = mock(NemIdCredentialsStatusUpdater.class);
        credentials = mock(Credentials.class);
        credentialsProvider = mock(NemIdCredentialsProvider.class);
        catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("whatever");
        supplementalInformationController = mock(SupplementalInformationController.class);

        loginPageStep =
                new NemIdLoginPageStep(
                        driverWrapper,
                        statusUpdater,
                        credentialsProvider,
                        catalog,
                        supplementalInformationController);

        mocksToVerifyInOrder =
                inOrder(driverWrapper, statusUpdater, supplementalInformationController);
    }

    @Test
    @Parameters(method = "params")
    public void should_ask_for_and_enter_credentials_then_click_login_and_update_status_payload(
            AskForCredentialsParams params) {
        // given
        params.mockCredentialsProvider(credentialsProvider);
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(params.getUserResponse());

        // when
        Throwable throwable = catchThrowable(() -> loginPageStep.login(credentials));

        // then
        if (params.isShouldAskUser()) {
            mocksToVerifyInOrder
                    .verify(supplementalInformationController)
                    .askSupplementalInformationSync(
                            params.getExpectedFieldsToAskUserFor(catalog).toArray(new Field[0]));
        }
        if (params.getExpectedException() != null) {
            assertThat(throwable)
                    .usingRecursiveComparison()
                    .isEqualTo(params.getExpectedException());
            mocksToVerifyInOrder.verifyNoMoreInteractions();
            return;
        }

        mocksToVerifyInOrder
                .verify(driverWrapper)
                .setValueToElement(params.getExpectedUserId(), USERNAME_INPUT);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .setValueToElement(params.getExpectedPassword(), PASSWORD_INPUT);
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(SUBMIT_BUTTON);

        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.VERIFYING_CREDS);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] params() {
        return Stream.of(
                        // don't ask user for anything
                        AskForCredentialsParams.builder()
                                .credentialsProviderUserId("userId1")
                                .credentialsProviderPassword("password1")
                                .shouldAskUser(false)
                                .expectedUserId("userId1")
                                .expectedPassword("password1")
                                .build(),
                        // ask for password
                        AskForCredentialsParams.builder()
                                .credentialsProviderUserId("userId")
                                .credentialsProviderPassword(null)
                                .shouldAskUser(true)
                                .expectedFieldsToAskUserFor(singletonList(NemIdPasswordField.NAME))
                                .userResponse(ImmutableMap.of(NemIdPasswordField.NAME, "password"))
                                .expectedUserId("userId")
                                .expectedPassword("password")
                                .build(),
                        // ask for userId, ignore password we didn't ask for
                        AskForCredentialsParams.builder()
                                .credentialsProviderUserId(null)
                                .credentialsProviderPassword("password1")
                                .shouldAskUser(true)
                                .expectedFieldsToAskUserFor(singletonList(NemIdUserIdField.NAME))
                                .userResponse(
                                        ImmutableMap.of(
                                                NemIdUserIdField.NAME, "userId",
                                                NemIdPasswordField.NAME, "password2"))
                                .expectedUserId("userId")
                                .expectedPassword("password1")
                                .build(),
                        // ask for userId and password
                        AskForCredentialsParams.builder()
                                .credentialsProviderUserId(null)
                                .credentialsProviderPassword(null)
                                .shouldAskUser(true)
                                .expectedFieldsToAskUserFor(
                                        asList(NemIdUserIdField.NAME, NemIdPasswordField.NAME))
                                .userResponse(
                                        ImmutableMap.of(
                                                NemIdUserIdField.NAME, "userId",
                                                NemIdPasswordField.NAME, "password"))
                                .expectedUserId("userId")
                                .expectedPassword("password")
                                .build(),
                        // ask for userId and password, throw on missing userId
                        AskForCredentialsParams.builder()
                                .credentialsProviderUserId(null)
                                .credentialsProviderPassword(null)
                                .shouldAskUser(true)
                                .expectedFieldsToAskUserFor(
                                        asList(NemIdUserIdField.NAME, NemIdPasswordField.NAME))
                                .userResponse(ImmutableMap.of(NemIdPasswordField.NAME, "password"))
                                .expectedException(
                                        LoginError.INCORRECT_CREDENTIALS.exception(
                                                "Missing NemID userId"))
                                .build(),
                        // ask for userId and password, throw on missing password
                        AskForCredentialsParams.builder()
                                .credentialsProviderUserId(null)
                                .credentialsProviderPassword(null)
                                .shouldAskUser(true)
                                .expectedFieldsToAskUserFor(
                                        asList(NemIdUserIdField.NAME, NemIdPasswordField.NAME))
                                .userResponse(ImmutableMap.of(NemIdUserIdField.NAME, "userId"))
                                .expectedException(
                                        LoginError.INCORRECT_CREDENTIALS.exception(
                                                "Missing NemID password"))
                                .build())
                .map(param -> new Object[] {param})
                .toArray();
    }

    @Getter
    @Builder
    private static class AskForCredentialsParams {
        private final String credentialsProviderUserId;
        private final String credentialsProviderPassword;

        private final boolean shouldAskUser;
        private final List<String> expectedFieldsToAskUserFor;
        private final Map<String, String> userResponse;

        private final String expectedUserId;
        private final String expectedPassword;
        private final Exception expectedException;

        private void mockCredentialsProvider(NemIdCredentialsProvider credentialsProvider) {
            when(credentialsProvider.getNemIdCredentials(any()))
                    .thenReturn(
                            NemIdCredentials.builder()
                                    .userId(credentialsProviderUserId)
                                    .password(credentialsProviderPassword)
                                    .build());
        }

        private List<Field> getExpectedFieldsToAskUserFor(Catalog catalog) {
            List<Field> fields = new ArrayList<>();
            if (expectedFieldsToAskUserFor.contains(NemIdUserIdField.NAME)) {
                fields.add(NemIdUserIdField.build(catalog));
            }
            if (expectedFieldsToAskUserFor.contains(NemIdPasswordField.NAME)) {
                fields.add(NemIdPasswordField.build(catalog));
            }
            return fields;
        }
    }
}
