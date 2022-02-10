package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ErrorMessages.Authentication.MIT_ID_NOT_SUPPORTED_YET;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ScaOptions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppTokenEncryptedPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppTokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.KeyCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.LoggedInEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.ScaOptionsEncryptedPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.SecondFactorOperationsEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdChoose2FAMethodField;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class BecAuthenticatorTest {

    private BecApiClient apiClient;
    private ApiClientMock apiClientMock;
    private SupplementalInformationController supplementalInformationController;
    private SupplementalInfoMock supplementalInfoMock;
    private Credentials credentials;
    private BecStorage storage;
    private Catalog catalog;
    private User user;
    private RandomValueGenerator randomValueGenerator;
    private InOrder mocksToVerifyInOrder;

    private BecAuthenticator authenticator;

    private static final String CREDENTIALS_USERNAME = "username_123";
    private static final String CREDENTIALS_PASSWORD = "password_123";

    private static final String PREVIOUSLY_SAVED_SCA_TOKEN = "previously_saved_sca_token";
    private static final String NEW_SCA_TOKEN = "new_sca_token";
    private static final String PREVIOUSLY_SAVED_DEVICE_ID = "6d94b480-5f84-4c0b-92bf-f41ffccd69fa";
    private static final String NEW_DEVICE_ID = "01f15b3f-5172-44bd-ab13-19b75ed12b39";

    private static final List<String> ALL_KNOWN_SCA_OPTIONS =
            asList(ScaOptions.CODEAPP_OPTION, ScaOptions.KEYCARD_OPTION, ScaOptions.MIT_ID_OPTION);
    private static final List<String> ALL_NEM_ID_SCA_OPTIONS =
            asList(ScaOptions.CODEAPP_OPTION, ScaOptions.KEYCARD_OPTION);
    private static final List<NemId2FAMethod> ALL_SUPPORTED_NEM_ID_METHODS =
            asList(NemId2FAMethod.CODE_APP, NemId2FAMethod.CODE_CARD);

    private static final String KEY_CARD_NUMBER = "key_card_number";
    private static final String NEM_ID_CHALLENGE = "nem_id_challenge";
    private static final String KEY_CARD_CODE_USER_RESPONSE = "key_card_code";

    private static final String NEM_ID_TOKEN = "nem_id_token";

    @Before
    public void setup() {
        prepareMocks();
        authenticator = createAuthenticator();
    }

    private void prepareMocks() {
        apiClient = mock(BecApiClient.class);
        apiClientMock = new ApiClientMock();

        supplementalInformationController = mock(SupplementalInformationController.class);
        supplementalInfoMock = new SupplementalInfoMock();

        catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class)))
                .thenReturn("some_irrelevant_translation");

        user = mock(User.class);

        randomValueGenerator = mock(RandomValueGenerator.class);
        when(randomValueGenerator.getUUID()).thenReturn(UUID.fromString(NEW_DEVICE_ID));

        storage = spy(new BecStorage(new PersistentStorage()));

        credentials = mock(Credentials.class);
        when(credentials.getField(Field.Key.USERNAME)).thenReturn(CREDENTIALS_USERNAME);
        when(credentials.getField(Field.Key.PASSWORD)).thenReturn(CREDENTIALS_PASSWORD);

        mocksToVerifyInOrder = inOrder(apiClient, supplementalInformationController);
    }

    private BecAuthenticator createAuthenticator() {
        return new BecAuthenticatorModule(
                        apiClient,
                        credentials,
                        storage,
                        user,
                        catalog,
                        supplementalInformationController,
                        randomValueGenerator)
                .createAuthenticator();
    }

    private void replaceStorage(BecStorage storage) {
        this.storage = storage;
        this.authenticator = createAuthenticator();
    }

    @Test
    @SneakyThrows
    public void should_auto_authenticate_reusing_previously_generated_device_id() {
        // given
        storage.saveDeviceId(PREVIOUSLY_SAVED_DEVICE_ID);
        storage.saveScaToken(PREVIOUSLY_SAVED_SCA_TOKEN);

        apiClientMock.mockAuthScaTokenResponseOk();

        // when
        authenticator.authenticate(credentials);

        // then
        assertThat(storage.getDeviceId()).isEqualTo(PREVIOUSLY_SAVED_DEVICE_ID);
        assertThat(storage.getScaToken()).isEqualTo(NEW_SCA_TOKEN);

        apiClientMock.verifyCallsAuthScaToken();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "parametersWithStorageRepresentingInvalidSession")
    public void
            should_throw_session_expired_when_session_is_invalid_and_user_is_not_available_for_interaction(
                    BecStorage storage) {
        // given
        replaceStorage(storage);
        when(user.isAvailableForInteraction()).thenReturn(false);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(throwable)
                .usingRecursiveComparison()
                .isEqualTo(SessionError.SESSION_EXPIRED.exception());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] parametersWithStorageRepresentingInvalidSession() {
        PersistentStorage emptyStorage = new PersistentStorage();

        PersistentStorage noDeviceId = new PersistentStorage();
        noDeviceId.put(StorageKeys.SCA_TOKEN_STORAGE_KEY, PREVIOUSLY_SAVED_SCA_TOKEN);

        PersistentStorage noScaToken = new PersistentStorage();
        noScaToken.put(StorageKeys.DEVICE_ID_STORAGE_KEY, PREVIOUSLY_SAVED_DEVICE_ID);

        return Stream.of(emptyStorage, noDeviceId, noScaToken).map(BecStorage::new).toArray();
    }

    @Test
    @Parameters(method = "parametersWithStorageRepresentingInvalidSession")
    public void
            should_continue_with_manual_auth_when_session_is_invalid_but_user_is_available_for_interaction(
                    BecStorage storage) {
        // given
        replaceStorage(storage);
        when(user.isAvailableForInteraction()).thenReturn(true);

        apiClientMock.mockScaOptionsResponseOk(ALL_KNOWN_SCA_OPTIONS);
        supplementalInfoMock.mockUserChoosesNemId2FAMethod(
                ALL_SUPPORTED_NEM_ID_METHODS, NemId2FAMethod.CODE_CARD);
        mockKeyCardHappyPath();

        // when
        authenticator.authenticate(credentials);

        // then
        apiClientMock.verifySyncsApp();
        apiClientMock.verifyFetchesScaOptions();
        supplementalInfoMock.verifyAsksUserToChoose2FAOption(ALL_SUPPORTED_NEM_ID_METHODS);
        verifyKeyCardHappyPath();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "parametersWithExemplaryNonLoginExceptions")
    public void
            should_rethrow_auto_refresh_token_non_login_exception_regardless_if_user_is_available_for_interaction(
                    Exception nonLoginException) {
        // given
        storage.saveDeviceId(PREVIOUSLY_SAVED_DEVICE_ID);
        storage.saveScaToken(PREVIOUSLY_SAVED_SCA_TOKEN);

        apiClientMock.mockAuthScaTokenResponseThrowsException(nonLoginException);

        for (boolean userAvailableForInteraction : asList(true, false)) {
            when(user.isAvailableForInteraction()).thenReturn(userAvailableForInteraction);

            // when
            Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

            // then
            assertThat(throwable).isEqualTo(nonLoginException);
        }
    }

    @SuppressWarnings("unused")
    private static Object[] parametersWithExemplaryNonLoginExceptions() {
        return new Object[] {mockHttpClientException(), new RuntimeException()};
    }

    @Test
    @Parameters(method = "parametersWithExemplaryLoginExceptions")
    public void
            should_rethrow_auto_refresh_token_login_exception_when_user_is_not_available_for_interaction(
                    LoginException loginException) {
        // given
        storage.saveDeviceId(PREVIOUSLY_SAVED_DEVICE_ID);
        storage.saveScaToken(PREVIOUSLY_SAVED_SCA_TOKEN);

        apiClientMock.mockAuthScaTokenResponseThrowsException(loginException);
        when(user.isAvailableForInteraction()).thenReturn(false);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(throwable).isEqualTo(loginException);
    }

    @SuppressWarnings("unused")
    private static Object[] parametersWithExemplaryLoginExceptions() {
        return new Object[] {
            LoginError.INCORRECT_CREDENTIALS.exception(), LoginError.DEFAULT_MESSAGE.exception()
        };
    }

    @Test
    @Parameters(method = "parametersWithExemplaryLoginExceptions")
    public void
            should_continue_with_manual_auth_after_auto_refresh_token_login_exception_when_user_is_available_for_interaction(
                    LoginException loginException) {
        // given
        storage.saveDeviceId(PREVIOUSLY_SAVED_DEVICE_ID);
        storage.saveScaToken(PREVIOUSLY_SAVED_SCA_TOKEN);
        when(user.isAvailableForInteraction()).thenReturn(true);

        apiClientMock.mockAuthScaTokenResponseThrowsException(loginException);

        apiClientMock.mockScaOptionsResponseOk(ALL_NEM_ID_SCA_OPTIONS);
        supplementalInfoMock.mockUserChoosesNemId2FAMethod(
                ALL_SUPPORTED_NEM_ID_METHODS, NemId2FAMethod.CODE_CARD);
        mockKeyCardHappyPath();

        // when
        authenticator.authenticate(credentials);

        // then
        apiClientMock.verifySyncsApp();
        apiClientMock.verifyFetchesScaOptions();
        supplementalInfoMock.verifyAsksUserToChoose2FAOption(ALL_SUPPORTED_NEM_ID_METHODS);
        verifyKeyCardHappyPath();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private static HttpClientException mockHttpClientException() {
        HttpRequest httpRequest = mock(HttpRequest.class);
        return new HttpClientException(httpRequest);
    }

    @Test
    public void should_throw_no_available_sca_methods_when_sca_options_is_empty() {
        // given
        storage.clearSessionData();
        when(user.isAvailableForInteraction()).thenReturn(true);

        apiClientMock.mockScaOptionsResponseOk(emptyList());

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(throwable)
                .usingRecursiveComparison()
                .isEqualTo(LoginError.NO_AVAILABLE_SCA_METHODS.exception());

        apiClientMock.verifySyncsApp();
        apiClientMock.verifyFetchesScaOptions();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "parametersWithOnlyUnknownScaOptions")
    public void should_throw_second_factor_not_registered_when_there_is_only_an_unknown_sca_option(
            List<String> onlyUnknownScaOptions) {
        // given
        storage.clearSessionData();
        when(user.isAvailableForInteraction()).thenReturn(true);

        apiClientMock.mockScaOptionsResponseOk(onlyUnknownScaOptions);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(throwable)
                .usingRecursiveComparison()
                .isEqualTo(NemIdError.SECOND_FACTOR_NOT_REGISTERED.exception());

        apiClientMock.verifySyncsApp();
        apiClientMock.verifyFetchesScaOptions();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] parametersWithOnlyUnknownScaOptions() {
        return new Object[] {
            new Object[] {singletonList("fasdgsdg")}, new Object[] {asList("sdfa124", "23^232")},
        };
    }

    @Test
    public void should_throw_mit_id_unsupported_when_mit_id_is_the_only_available_sca_option() {
        // given
        storage.clearSessionData();
        when(user.isAvailableForInteraction()).thenReturn(true);

        apiClientMock.mockScaOptionsResponseOk(ScaOptions.MIT_ID_OPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(throwable)
                .usingRecursiveComparison()
                .isEqualTo(LoginError.NO_AVAILABLE_SCA_METHODS.exception(MIT_ID_NOT_SUPPORTED_YET));

        apiClientMock.verifySyncsApp();
        apiClientMock.verifyFetchesScaOptions();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "parametersWithAllNemIdSCAOptions")
    public void should_not_ask_user_to_choose_2fa_method_when_only_1_method_is_available(
            String scaOption) {
        // given
        storage.clearSessionData();
        when(user.isAvailableForInteraction()).thenReturn(true);

        apiClientMock.mockScaOptionsResponseOk(scaOption);

        switch (scaOption) {
            case ScaOptions.CODEAPP_OPTION:
                mockCodeAppHappyPath(true);
                break;
            case ScaOptions.KEYCARD_OPTION:
                mockKeyCardHappyPath();
                break;
            default:
                throw new IllegalStateException("Test misconfiguration");
        }

        // when
        authenticator.authenticate(credentials);

        // then
        apiClientMock.verifySyncsApp();
        apiClientMock.verifyFetchesScaOptions();
        switch (scaOption) {
            case ScaOptions.CODEAPP_OPTION:
                verifyCodeAppHappyPath();
                break;
            case ScaOptions.KEYCARD_OPTION:
                verifyKeyCardHappyPath();
        }
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] parametersWithAllNemIdSCAOptions() {
        return ALL_NEM_ID_SCA_OPTIONS.toArray();
    }

    @Test
    @Parameters(method = "parametersWithAllNemIdSCAOptions")
    public void should_ask_user_to_choose_2fa_method_and_authenticate_with_it(String scaOption) {
        // given
        storage.clearSessionData();
        when(user.isAvailableForInteraction()).thenReturn(true);

        apiClientMock.mockScaOptionsResponseOk(ALL_KNOWN_SCA_OPTIONS);
        switch (scaOption) {
            case ScaOptions.CODEAPP_OPTION:
                supplementalInfoMock.mockUserChoosesNemId2FAMethod(
                        ALL_SUPPORTED_NEM_ID_METHODS, NemId2FAMethod.CODE_APP);
                mockCodeAppHappyPath(true);
                break;
            case ScaOptions.KEYCARD_OPTION:
                supplementalInfoMock.mockUserChoosesNemId2FAMethod(
                        ALL_SUPPORTED_NEM_ID_METHODS, NemId2FAMethod.CODE_CARD);
                mockKeyCardHappyPath();
                break;
            default:
                throw new IllegalStateException("Test misconfiguration");
        }

        // when
        authenticator.authenticate(credentials);

        // then
        apiClientMock.verifySyncsApp();
        apiClientMock.verifyFetchesScaOptions();
        supplementalInfoMock.verifyAsksUserToChoose2FAOption(ALL_SUPPORTED_NEM_ID_METHODS);
        switch (scaOption) {
            case ScaOptions.CODEAPP_OPTION:
                verifyCodeAppHappyPath();
                break;
            case ScaOptions.KEYCARD_OPTION:
                verifyKeyCardHappyPath();
        }
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_throw_supplemental_info_exception_when_user_does_not_provide_key_card_code() {
        // given
        storage.clearSessionData();
        when(user.isAvailableForInteraction()).thenReturn(true);

        apiClientMock.mockScaOptionsResponseOk(ALL_KNOWN_SCA_OPTIONS);
        supplementalInfoMock.mockUserChoosesNemId2FAMethod(
                ALL_SUPPORTED_NEM_ID_METHODS, NemId2FAMethod.CODE_CARD);

        apiClientMock.mockKeyCardValuesResponseOk();
        supplementalInfoMock.mockUserDoesNotProvideKeyCardCode(
                ImmutableMap.of("some_other_field_key", "irrelevant_value"));

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(throwable)
                .usingRecursiveComparison()
                .isEqualTo(SupplementalInfoError.NO_VALID_CODE.exception());

        apiClientMock.verifySyncsApp();
        apiClientMock.verifyFetchesScaOptions();
        supplementalInfoMock.verifyAsksUserToChoose2FAOption(ALL_SUPPORTED_NEM_ID_METHODS);
        apiClientMock.verifyFetchesKeyCardValues();
        supplementalInfoMock.verifyAsksUserToProvideKeyCardCode();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_ignore_when_user_doesnt_click_code_app_prompt_and_continue_authentication() {
        // given
        storage.clearSessionData();
        when(user.isAvailableForInteraction()).thenReturn(true);

        apiClientMock.mockScaOptionsResponseOk(ALL_KNOWN_SCA_OPTIONS);
        supplementalInfoMock.mockUserChoosesNemId2FAMethod(
                ALL_SUPPORTED_NEM_ID_METHODS, NemId2FAMethod.CODE_APP);
        mockCodeAppHappyPath(false);

        // when
        authenticator.authenticate(credentials);

        // then
        apiClientMock.verifySyncsApp();
        apiClientMock.verifyFetchesScaOptions();
        supplementalInfoMock.verifyAsksUserToChoose2FAOption(ALL_SUPPORTED_NEM_ID_METHODS);
        verifyCodeAppHappyPath();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockKeyCardHappyPath() {
        apiClientMock.mockKeyCardValuesResponseOk();
        supplementalInfoMock.mockUserProvidesKeyCardCode();

        apiClientMock.mockAuthenticateWithKeyCardResponse();
    }

    private void verifyKeyCardHappyPath() {
        apiClientMock.verifyFetchesKeyCardValues();
        supplementalInfoMock.verifyAsksUserToProvideKeyCardCode();

        apiClientMock.verifyAuthenticatesWithKeyCardCode();
        assertThat(storage.getScaToken()).isEqualTo(NEW_SCA_TOKEN);
        assertThat(storage.getDeviceId()).isEqualTo(NEW_DEVICE_ID);
    }

    private void mockCodeAppHappyPath(boolean userAcceptsPrompt) {
        // init code app authentication
        apiClientMock.mockGetNemIdTokenResponse();

        if (userAcceptsPrompt) {
            supplementalInfoMock.mockUserAcceptsCodeAppPrompt();
        } else {
            supplementalInfoMock.mockUserDoesNotAcceptCodeAppPrompt();
        }

        // we get SCA token
        apiClientMock.mockExchangeNemIdTokenForScaTokenResponse();
    }

    private void verifyCodeAppHappyPath() {
        // init code app authentication
        apiClientMock.verifyFetchesNemIdToken();

        // user accepts
        supplementalInfoMock.verifyDisplaysCodeAppPrompt();

        // we poll to check status
        apiClientMock.verifyPollsNemIdTokenStatus();

        // we get SCA token
        apiClientMock.verifyExchangesNemIdTokenForScaToken();
        assertThat(storage.getScaToken()).isEqualTo(NEW_SCA_TOKEN);
        assertThat(storage.getDeviceId()).isEqualTo(NEW_DEVICE_ID);
    }

    private class ApiClientMock {

        // ---------
        // Sync app
        // ---------
        private void verifySyncsApp() {
            mocksToVerifyInOrder.verify(apiClient).appSync();
        }

        // ---------
        // Authenticate with SCA token
        // ---------
        private void mockAuthScaTokenResponseOk() {
            when(apiClient.authScaToken(any(), any(), any(), any()))
                    .thenReturn(LoggedInEntity.builder().scaToken(NEW_SCA_TOKEN).build());
        }

        private void mockAuthScaTokenResponseThrowsException(Exception exception) {
            when(apiClient.authScaToken(any(), any(), any(), any())).thenThrow(exception);
        }

        private void verifyCallsAuthScaToken() {
            mocksToVerifyInOrder
                    .verify(apiClient)
                    .authScaToken(
                            CREDENTIALS_USERNAME,
                            CREDENTIALS_PASSWORD,
                            PREVIOUSLY_SAVED_SCA_TOKEN,
                            PREVIOUSLY_SAVED_DEVICE_ID);
        }

        // ---------
        // Fetch SCA options
        // ---------
        private void mockScaOptionsResponseOk(String... scaOptions) {
            mockScaOptionsResponseOk(asList(scaOptions));
        }

        private void mockScaOptionsResponseOk(List<String> scaOptions) {
            when(apiClient.getScaOptions(any(), any(), any()))
                    .thenReturn(new ScaOptionsEncryptedPayload(scaOptions));
        }

        private void verifyFetchesScaOptions() {
            mocksToVerifyInOrder
                    .verify(apiClient)
                    .getScaOptions(CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD, NEW_DEVICE_ID);
        }

        // ---------
        // Fetch key card values
        // ---------
        private void mockKeyCardValuesResponseOk() {
            when(apiClient.postKeyCardValuesAndDecryptResponse(any(), any(), any()))
                    .thenReturn(
                            SecondFactorOperationsEntity.builder()
                                    .keycard(
                                            KeyCardEntity.builder()
                                                    .keycardNo(KEY_CARD_NUMBER)
                                                    .nemidChallenge(NEM_ID_CHALLENGE)
                                                    .build())
                                    .build());
        }

        private void verifyFetchesKeyCardValues() {
            mocksToVerifyInOrder
                    .verify(apiClient)
                    .postKeyCardValuesAndDecryptResponse(
                            CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD, NEW_DEVICE_ID);
        }

        // ---------
        // Authenticate with key card code
        // ---------
        private void mockAuthenticateWithKeyCardResponse() {
            when(apiClient.authKeyCard(any(), any(), any(), any(), any()))
                    .thenReturn(LoggedInEntity.builder().scaToken(NEW_SCA_TOKEN).build());
        }

        private void verifyAuthenticatesWithKeyCardCode() {
            mocksToVerifyInOrder
                    .verify(apiClient)
                    .authKeyCard(
                            CREDENTIALS_USERNAME,
                            CREDENTIALS_PASSWORD,
                            KEY_CARD_CODE_USER_RESPONSE,
                            NEM_ID_CHALLENGE,
                            NEW_DEVICE_ID);
        }

        // ---------
        // Fetch NemID token
        // ---------
        private void mockGetNemIdTokenResponse() {
            CodeAppTokenEncryptedPayload payload =
                    CodeAppTokenEncryptedPayload.builder()
                            .codeappTokenDetails(
                                    CodeAppTokenEntity.builder().token(NEM_ID_TOKEN).build())
                            .build();
            when(apiClient.getNemIdToken(any(), any(), any())).thenReturn(payload);
        }

        private void verifyFetchesNemIdToken() {
            mocksToVerifyInOrder
                    .verify(apiClient)
                    .getNemIdToken(CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD, NEW_DEVICE_ID);
        }

        // ---------
        // Poll NemID token status
        // ---------
        private void verifyPollsNemIdTokenStatus() {
            mocksToVerifyInOrder.verify(apiClient).pollNemId(NEM_ID_TOKEN);
        }

        // ---------
        // Authenticate with NemID token
        // ---------
        private void mockExchangeNemIdTokenForScaTokenResponse() {
            when(apiClient.authCodeApp(any(), any(), any(), any()))
                    .thenReturn(LoggedInEntity.builder().scaToken(NEW_SCA_TOKEN).build());
        }

        private void verifyExchangesNemIdTokenForScaToken() {
            mocksToVerifyInOrder
                    .verify(apiClient)
                    .authCodeApp(
                            CREDENTIALS_USERNAME,
                            CREDENTIALS_PASSWORD,
                            NEM_ID_TOKEN,
                            NEW_DEVICE_ID);
        }
    }

    private class SupplementalInfoMock {

        // ---------
        // Choose 2FA option
        // ---------
        @SuppressWarnings("SameParameterValue")
        private void mockUserChoosesNemId2FAMethod(
                List<NemId2FAMethod> optionsForUser, NemId2FAMethod chosenMethod) {
            Field chooseMethodField = prepareChooseNemIdMethodField(optionsForUser);
            when(supplementalInformationController.askSupplementalInformationSync(
                            chooseMethodField))
                    .thenReturn(
                            ImmutableMap.of(
                                    NemIdChoose2FAMethodField.FIELD_KEY,
                                    chosenMethod.getSupplementalInfoKey()));
        }

        private Field prepareChooseNemIdMethodField(List<NemId2FAMethod> optionsForUser) {
            List<NemId2FAMethod> sortedMethodsList =
                    optionsForUser.stream()
                            .sorted(Comparator.comparing(NemId2FAMethod::getSupplementalInfoOrder))
                            .collect(Collectors.toList());

            return NemIdChoose2FAMethodField.build(catalog, sortedMethodsList);
        }

        @SuppressWarnings("SameParameterValue")
        private void verifyAsksUserToChoose2FAOption(List<NemId2FAMethod> optionsForUser) {
            mocksToVerifyInOrder
                    .verify(supplementalInformationController)
                    .askSupplementalInformationSync(prepareChooseNemIdMethodField(optionsForUser));
        }

        // ---------
        // Choose 2FA option
        // ---------
        private void mockUserProvidesKeyCardCode() {
            Field keyCardInfoField =
                    CommonFields.KeyCardInfo.build(catalog, KEY_CARD_NUMBER, NEM_ID_CHALLENGE);
            Field keyCardCodeField = CommonFields.KeyCardCode.build(catalog, 6);
            when(supplementalInformationController.askSupplementalInformationSync(
                            keyCardInfoField, keyCardCodeField))
                    .thenReturn(
                            ImmutableMap.of(
                                    keyCardCodeField.getName(), KEY_CARD_CODE_USER_RESPONSE));
        }

        private void mockUserDoesNotProvideKeyCardCode(Map<String, String> userResponse) {
            Field keyCardInfoField =
                    CommonFields.KeyCardInfo.build(catalog, KEY_CARD_NUMBER, NEM_ID_CHALLENGE);
            Field keyCardCodeField = CommonFields.KeyCardCode.build(catalog, 6);
            when(supplementalInformationController.askSupplementalInformationSync(
                            keyCardInfoField, keyCardCodeField))
                    .thenReturn(userResponse);
        }

        private void verifyAsksUserToProvideKeyCardCode() {
            Field keyCardInfoField =
                    CommonFields.KeyCardInfo.build(catalog, KEY_CARD_NUMBER, NEM_ID_CHALLENGE);
            Field keyCardCodeField = CommonFields.KeyCardCode.build(catalog, 6);
            mocksToVerifyInOrder
                    .verify(supplementalInformationController)
                    .askSupplementalInformationSync(keyCardInfoField, keyCardCodeField);
        }

        // ---------
        // Display code app prompt
        // ---------
        private void mockUserAcceptsCodeAppPrompt() {
            Field field = prepareCodeAppPromptField();
            when(supplementalInformationController.askSupplementalInformationSync(field))
                    .thenReturn(Collections.emptyMap());
        }

        private void mockUserDoesNotAcceptCodeAppPrompt() {
            Field field = prepareCodeAppPromptField();
            when(supplementalInformationController.askSupplementalInformationSync(field))
                    .thenThrow(SupplementalInfoError.NO_VALID_CODE.exception());
        }

        private Field prepareCodeAppPromptField() {
            return DanishFields.NemIdInfo.build(catalog);
        }

        private void verifyDisplaysCodeAppPrompt() {
            Field field = prepareCodeAppPromptField();
            mocksToVerifyInOrder
                    .verify(supplementalInformationController)
                    .askSupplementalInformationSync(field);
        }
    }
}
