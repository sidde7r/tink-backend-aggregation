package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail.PostbankEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail.PostbankIconUrlMapper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail.PostbankPaymentsEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.UpdateAuthorisationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.crypto.PostbankFakeJwtGenerator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Parameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PostbankAuthenticationControllerTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/postbank/resources";

    private static final String CONSENT_DETAILS_EXPIRED = "consent_details_response_expired.json";
    private static final String CONSENT_DETAILS_VALID = "consent_details_response_valid.json";

    private static final String AUTH_RESP = "authorisation_response.json";
    private static final String AUTH_RESP_UNSUPPORTED = "authorisation_response_unsupported.json";
    private static final String AUTH_RESP_SELECTED = "authorisation_response_selected.json";
    private static final String AUTH_RESP_CHIP_TAN = "authorisation_response_chip_tan.json";
    private static final String AUTH_RESP_CHIP_TAN_SELECTED =
            "authorisation_response_chip_tan_selected.json";
    private static final String AUTH_RESP_SINGLE_UNSUPPORTED =
            "authorisation_response_single_unsupported.json";
    private static final String AUTH_RESP_FINALISED = "authorisation_response_status.json";
    private static final String PAYMENT_INITIALIZED = "payment_initialized.json";

    private static final String URL_AUTH_TRANSACTION =
            "https://xs2a.db.com/ais/DE/Postbank/v1/consents/authoriseTransaction";
    private static final String URL_SCA =
            "https://xs2a.db.com/ais/DE/Postbank/v1/consents/scaStatus";

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String OTP_CODE = "otpCode";

    private final Catalog catalog = new Catalog(Locale.getDefault());
    private final RandomValueGenerator randomValueGenerator = new RandomValueGeneratorImpl();

    private final SupplementalInformationController mockSuppController =
            mock(SupplementalInformationController.class);

    @Captor ArgumentCaptor<Field> fieldCaptor;

    @Test
    public void when_one_sca_available_then_should_finish_without_selection() {
        // given
        PostbankAuthenticator mockAuthenticator = mock(PostbankAuthenticator.class);

        when(mockAuthenticator.init(USERNAME, PASSWORD))
                .thenReturn(getAuthorizationResponse(AUTH_RESP_SELECTED));
        when(mockAuthenticator.authoriseWithOtp(OTP_CODE, USERNAME, URL_AUTH_TRANSACTION))
                .thenReturn(getAuthorizationResponse(AUTH_RESP_FINALISED));

        mockSupplementalInfoController(AUTH_RESP_SELECTED, "1");

        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        catalog,
                        mockSuppController,
                        mockAuthenticator,
                        new PostbankEmbeddedFieldBuilder(catalog, new PostbankIconUrlMapper()),
                        new MockRandomValueGenerator());

        // when
        postbankAuthenticationController.authenticate(createCredentials(null));

        // then
        verify(mockSuppController).askSupplementalInformationSync(any());
    }

    @Test
    public void when_multiple_sca_available_then_should_finish_with_selection() {
        // given
        PostbankAuthenticator mockAuthenticator = mock(PostbankAuthenticator.class);

        when(mockAuthenticator.init(USERNAME, PASSWORD))
                .thenReturn(getAuthorizationResponse(AUTH_RESP));
        when(mockAuthenticator.selectScaMethod("1", USERNAME, URL_SCA))
                .thenReturn(getAuthorizationResponse(AUTH_RESP_SELECTED));
        when(mockAuthenticator.authoriseWithOtp(OTP_CODE, USERNAME, URL_AUTH_TRANSACTION))
                .thenReturn(getAuthorizationResponse(AUTH_RESP_FINALISED));

        mockSupplementalInfoController(AUTH_RESP_SELECTED, "1");

        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        catalog,
                        mockSuppController,
                        mockAuthenticator,
                        new PostbankEmbeddedFieldBuilder(catalog, new PostbankIconUrlMapper()),
                        new MockRandomValueGenerator());

        // when
        postbankAuthenticationController.authenticate(createCredentials(null));

        // then
        verify(mockSuppController, times(2)).askSupplementalInformationSync(any());
    }

    @Test
    public void when_one_unsupported_sca_available_then_should_throw() {
        // given
        PostbankAuthenticator mockAuthenticator = mock(PostbankAuthenticator.class);

        when(mockAuthenticator.init(USERNAME, PASSWORD))
                .thenReturn(getAuthorizationResponse(AUTH_RESP_SINGLE_UNSUPPORTED));

        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        catalog,
                        mockSuppController,
                        mockAuthenticator,
                        new PostbankEmbeddedFieldBuilder(catalog, new PostbankIconUrlMapper()),
                        new MockRandomValueGenerator());
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                postbankAuthenticationController.authenticate(
                                        createCredentials(null)));

        // then
        assertThat(thrown)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NO_AVAILABLE_SCA_METHODS");
    }

    @Test
    public void when_multiple_unsupported_sca_available_then_should_throw() {
        // given
        PostbankAuthenticator mockAuthenticator = mock(PostbankAuthenticator.class);

        when(mockAuthenticator.init(USERNAME, PASSWORD))
                .thenReturn(getAuthorizationResponse(AUTH_RESP_UNSUPPORTED));

        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        new Catalog(Locale.getDefault()),
                        mockSuppController,
                        mockAuthenticator,
                        new PostbankEmbeddedFieldBuilder(catalog, new PostbankIconUrlMapper()),
                        new MockRandomValueGenerator());

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                postbankAuthenticationController.authenticate(
                                        createCredentials(null)));

        // then
        assertThat(thrown)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NO_AVAILABLE_SCA_METHODS");
    }

    @Test
    public void when_user_is_authenticated_then_session_expiry_date_is_set() {
        // given
        Date date = toDate("2030-01-01");
        TinkHttpClient tinkHttpClient = mockHttpClient(CONSENT_DETAILS_VALID);
        Credentials credentials = createCredentials(null);

        PostbankAuthenticator postbankAuthenticator =
                createPostbankAuthenticator(tinkHttpClient, new PersistentStorage(), credentials);

        PostbankAuthenticationController postbankAuthenticationController =
                createAutoAuthenticationController(postbankAuthenticator);

        // when
        postbankAuthenticationController.authenticate(credentials);

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(date);
    }

    @Test
    public void when_user_rejects_consent_then_cancelled_is_thrown() {
        // given
        TinkHttpClient tinkHttpClient = mockHttpClient(CONSENT_DETAILS_EXPIRED);
        Credentials credentials = createCredentials(null);

        PostbankAuthenticator postbankAuthenticator =
                createPostbankAuthenticator(tinkHttpClient, new PersistentStorage(), credentials);

        PostbankAuthenticationController postbankAuthenticationController =
                createAutoAuthenticationController(postbankAuthenticator);

        // when

        Throwable thrown =
                catchThrowable(() -> postbankAuthenticationController.authenticate(credentials));

        // then
        assertThat(thrown)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }

    @Test
    public void when_consent_is_not_valid_auto_authenticate_throws_session_exception() {
        // given
        TinkHttpClient tinkHttpClient = mockHttpClient(CONSENT_DETAILS_EXPIRED);
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(StorageKeys.CONSENT_ID, "consentId");
        Credentials credentials = createCredentials(new Date());

        PostbankAuthenticator autoAuthenticator =
                createPostbankAuthenticator(tinkHttpClient, persistentStorage, credentials);

        // when
        Throwable thrown = catchThrowable(autoAuthenticator::autoAuthenticate);

        // then
        assertThat(thrown).isInstanceOf(SessionException.class);
    }

    @Test
    public void when_consentId_is_not_in_storage_auto_authenticate_throws_session_exception() {
        // given
        TinkHttpClient tinkHttpClient = mockHttpClient(CONSENT_DETAILS_EXPIRED);
        PersistentStorage persistentStorage = new PersistentStorage();
        Credentials credentials = createCredentials(new Date());

        PostbankAuthenticator autoAuthenticator =
                createPostbankAuthenticator(tinkHttpClient, persistentStorage, credentials);

        // when
        Throwable thrown = catchThrowable(autoAuthenticator::autoAuthenticate);

        // then
        assertThat(thrown).isInstanceOf(SessionException.class);
    }

    @Test
    public void when_auto_authenticate_then_session_expiry_date_should_be_set() {
        // given
        Date currentSessionExpiryDate = toDate("2029-01-01");
        Date newSessionExpiryDate = toDate("2030-01-01");

        TinkHttpClient tinkHttpClient = mockHttpClient(CONSENT_DETAILS_VALID);
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(StorageKeys.CONSENT_ID, "consentId");
        Credentials credentials = createCredentials(currentSessionExpiryDate);

        PostbankAuthenticator autoAuthenticator =
                createPostbankAuthenticator(tinkHttpClient, persistentStorage, credentials);

        // when
        autoAuthenticator.autoAuthenticate();

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(newSessionExpiryDate);
    }

    @Test
    public void authenticate_payment_should_complete_with_chip_tan_selected() {
        // given
        PostbankAuthenticator mockAuthenticator = mock(PostbankAuthenticator.class);

        when(mockAuthenticator.startAuthorsation(
                        "/v1/payments/pain.001-sepa-credit-transfers/asdf/authorisations",
                        USERNAME,
                        PASSWORD))
                .thenReturn(getAuthorizationResponse(AUTH_RESP_CHIP_TAN));
        when(mockAuthenticator.selectScaMethod("2", USERNAME, URL_SCA))
                .thenReturn(getAuthorizationResponse(AUTH_RESP_CHIP_TAN_SELECTED));
        when(mockAuthenticator.authoriseWithOtp(OTP_CODE, USERNAME, URL_AUTH_TRANSACTION))
                .thenReturn(getAuthorizationResponse(AUTH_RESP_FINALISED));

        mockSupplementalInfoController(AUTH_RESP_CHIP_TAN_SELECTED, "2");

        PostbankPaymentAuthenticator postbankPaymentAuthenticator =
                createPostbankPaymentAuthenticator(mockAuthenticator);

        fieldCaptor = ArgumentCaptor.forClass(Field.class);

        // when
        postbankPaymentAuthenticator.authenticatePayment(
                SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, PAYMENT_INITIALIZED).toFile(),
                                CreatePaymentResponse.class)
                        .getLinks());

        // and verify supplement interactions
        verify(mockSuppController, times(2)).askSupplementalInformationSync(fieldCaptor.capture());
        List<Field> allValues = fieldCaptor.getAllValues();
        assertThat(allValues).hasSize(5);
        assertThat(allValues.get(0).getName()).isEqualTo("selectAuthMethodField");
        assertThat(allValues.get(1).getName()).isEqualTo("TEMPLATE");
        assertThat(allValues.get(1).getValue()).isEqualTo("CARD_READER");
        assertThat(allValues.get(2).getName()).isEqualTo("instruction");
        assertThat(allValues.get(2).getValue()).isEqualTo("123456");
        assertThat(allValues.get(3).getName()).isEqualTo("chipTan");
        assertThat(allValues.get(4).getName()).isEqualTo("instructionList");
        assertThat(allValues.get(4).getValue())
                .isEqualTo(
                        "[\"Bitte legen Sie die Debitkarte in Ihren TAN-Generator und drücken Sie die TAN-Taste auf dem TAN-Generator.\",\"Tippen Sie den folgenden Startcode in Ihren TAN-Generator ein und drücken Sie die OK-Taste.\",\"Startcode: 123456\",\"Geben Sie die Auftragsdaten in den TAN-Generator ein.\",\"Ihre Eingabe:\",\"kontonummer: DE32701694660000123456\",\"betrag: 0,10\",\"\",\"Tragen Sie nun die erzeugte ChipTAN in das Eingabefeld ein und geben Sie den Auftrag frei.\",\"Bitte geben Sie im Feld Konto/IBAN nur die ersten 10 Ziffern der IBAN ein. Beispiel: DE93 5001 AZ78 9012 3456 78 Eingabe: 9350017890\"]");
    }

    private void mockSupplementalInfoController(
            String authorizationResponseFile, String authMethod) {
        Map<String, String> supplementalInformation = new HashMap<>();
        supplementalInformation.put(
                getFieldName(
                        getAuthorizationResponse(authorizationResponseFile).getChosenScaMethod()),
                OTP_CODE);
        supplementalInformation.put("selectAuthMethodField", authMethod);
        when(mockSuppController.askSupplementalInformationSync(any()))
                .thenReturn(supplementalInformation);
    }

    private Credentials createCredentials(Date sessionExpiryDate) {
        Credentials credentials = new Credentials();
        credentials.setUsername(USERNAME);
        credentials.setPassword(PASSWORD);
        credentials.setSessionExpiryDate(sessionExpiryDate);
        credentials.setType(CredentialsTypes.PASSWORD);
        return credentials;
    }

    private PostbankAuthenticator createPostbankAuthenticator(
            TinkHttpClient tinkHttpClient,
            PersistentStorage persistentStorage,
            Credentials credentials) {

        PostbankApiClient postbankApiClient =
                createPostbankApiClient(tinkHttpClient, persistentStorage);
        return new PostbankAuthenticator(postbankApiClient, persistentStorage, credentials);
    }

    private PostbankApiClient createPostbankApiClient(
            TinkHttpClient tinkHttpClient, PersistentStorage persistentStorage) {
        DeutscheHeaderValues deutscheHeaderValues =
                new DeutscheHeaderValues("redirectUrl", "userIp");
        DeutscheMarketConfiguration deutscheMarketConfiguration =
                new DeutscheMarketConfiguration(
                        "baseUrl/{" + Parameters.SERVICE_KEY + "}", "psuIdType");
        PostbankApiClient postbankApiClient =
                new PostbankApiClient(
                        tinkHttpClient,
                        persistentStorage,
                        deutscheHeaderValues,
                        deutscheMarketConfiguration,
                        randomValueGenerator,
                        new ActualLocalDateTimeSource());
        postbankApiClient.enrichWithJwtGenerator(new PostbankFakeJwtGenerator());
        return postbankApiClient;
    }

    private PostbankPaymentAuthenticator createPostbankPaymentAuthenticator(
            PostbankAuthenticator postbankAuthenticator) {
        return new PostbankPaymentAuthenticator(
                catalog,
                mockSuppController,
                postbankAuthenticator,
                createCredentials(toDate("2029-01-01")),
                new PostbankPaymentsEmbeddedFieldBuilder(catalog, new PostbankIconUrlMapper()),
                new MockRandomValueGenerator());
    }

    private PostbankAuthenticationController createAutoAuthenticationController(
            PostbankAuthenticator postbankAuthenticator) {
        mockSupplementalInfoController(AUTH_RESP_SELECTED, "3");

        return new PostbankAuthenticationController(
                catalog,
                mockSuppController,
                postbankAuthenticator,
                new PostbankEmbeddedFieldBuilder(catalog, new PostbankIconUrlMapper()),
                new MockRandomValueGenerator());
    }

    private String getFieldName(ScaMethodEntity scaMethod) {
        if (scaMethod != null) {
            AuthenticationType authenticationType =
                    AuthenticationType.fromString(scaMethod.getAuthenticationType()).get();
            switch (authenticationType) {
                case CHIP_OTP:
                    return "chipTan";
                case SMS_OTP:
                    return "smsTan";
                case PUSH_OTP:
                    return "pushTan";
            }
        }
        return "tanField";
    }

    private Date toDate(String date) {
        return Date.from(
                LocalDate.parse(date).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public TinkHttpClient mockHttpClient(String consentDetails) {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class))).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class), anyString())).thenReturn(requestBuilder);

        when(requestBuilder.post(any(), any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consent_response.json").toFile(),
                                ConsentResponse.class));
        when(requestBuilder.put(any(), anyString()))
                .thenReturn(getAuthorizationResponse(AUTH_RESP));
        when(requestBuilder.put(any(), any(UpdateAuthorisationRequest.class)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "authorisation_response_updated.json")
                                        .toFile(),
                                AuthorizationResponse.class));

        when(requestBuilder.get(AuthorizationResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "authorisation_response_status.json")
                                        .toFile(),
                                AuthorizationResponse.class));

        when(requestBuilder.get(ConsentDetailsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, consentDetails).toFile(),
                                ConsentDetailsResponse.class));

        when(requestBuilder.get(ConsentResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consent_status_response_valid.json")
                                        .toFile(),
                                ConsentResponse.class));

        return tinkHttpClient;
    }

    private AuthorizationResponse getAuthorizationResponse(String filename) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), AuthorizationResponse.class);
    }
}
