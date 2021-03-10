package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.VarargMatcher;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.ScaMethod;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.UpdateAuthorisationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PostbankAuthenticationControllerTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/postbank/resources";

    private static final String CONSENT_DETAILS_EXPIRED = "consent_details_response_expired.json";
    private static final String CONSENT_DETAILS_VALID = "consent_details_response_valid.json";

    private static final String AUTH_RESP = "authorisation_response.json";
    private static final String AUTH_RESP_UNSUPPORTED = "authorisation_response_unsupported.json";
    private static final String AUTH_RESP_SINGLE = "authorisation_response_single.json";
    private static final String AUTH_RESP_SINGLE_UNSUPPORTED =
            "authorisation_response_single_unsupported.json";
    private static final String AUTH_RESP_FINALISED = "authorisation_response_status.json";

    private static final String URL_AUTH_TRANSACTION =
            "https://xs2a.db.com/ais/DE/Postbank/v1/consents/authoriseTransaction";
    private static final String URL_SCA =
            "https://xs2a.db.com/ais/DE/Postbank/v1/consents/scaStatus";

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String OTP_CODE = "otpCode";

    private final Catalog catalog = new Catalog(Locale.getDefault());

    private final SupplementalInformationController mockSuppController =
            mock(SupplementalInformationController.class);

    @Test
    public void when_one_sca_available_then_should_finish_without_selection() {
        // given
        PostbankAuthenticator mockAuthenticator = mock(PostbankAuthenticator.class);

        when(mockAuthenticator.init(USERNAME, PASSWORD))
                .thenReturn(getAuthorisationResponse(AUTH_RESP_SINGLE));
        when(mockAuthenticator.authoriseWithOtp(OTP_CODE, USERNAME, URL_AUTH_TRANSACTION))
                .thenReturn(getAuthorisationResponse(AUTH_RESP_FINALISED));

        mockProvidingOtpCode(getAuthorisationResponse(AUTH_RESP_SINGLE));

        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        catalog, mockSuppController, mockAuthenticator);

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
                .thenReturn(getAuthorisationResponse(AUTH_RESP));
        when(mockAuthenticator.selectScaMethod("1", USERNAME, URL_SCA))
                .thenReturn(getAuthorisationResponse(AUTH_RESP_SINGLE));
        when(mockAuthenticator.authoriseWithOtp(OTP_CODE, USERNAME, URL_AUTH_TRANSACTION))
                .thenReturn(getAuthorisationResponse(AUTH_RESP_FINALISED));

        mockScaMethodSelection(getAuthorisationResponse(AUTH_RESP), 1);
        mockProvidingOtpCode(getAuthorisationResponse(AUTH_RESP_SINGLE));

        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        catalog, mockSuppController, mockAuthenticator);

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
                .thenReturn(getAuthorisationResponse(AUTH_RESP_SINGLE_UNSUPPORTED));

        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        catalog, mockSuppController, mockAuthenticator);
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
                .thenReturn(getAuthorisationResponse(AUTH_RESP_UNSUPPORTED));

        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        new Catalog(Locale.getDefault()), mockSuppController, mockAuthenticator);

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

        DeutscheHeaderValues deutscheHeaderValues =
                new DeutscheHeaderValues("redirectUrl", "userIp");
        DeutscheMarketConfiguration deutscheMarketConfiguration =
                new DeutscheMarketConfiguration("baseUrl", "psuIdType");
        PostbankApiClient postbankApiClient =
                new PostbankApiClient(
                        tinkHttpClient,
                        persistentStorage,
                        deutscheHeaderValues,
                        deutscheMarketConfiguration);
        return new PostbankAuthenticator(postbankApiClient, persistentStorage, credentials);
    }

    private PostbankAuthenticationController createAutoAuthenticationController(
            PostbankAuthenticator postbankAuthenticator) {
        AuthorisationResponse authorisationResponse = getAuthorisationResponse(AUTH_RESP);
        mockScaMethodSelection(authorisationResponse, 3);

        return new PostbankAuthenticationController(
                catalog, mockSuppController, postbankAuthenticator);
    }

    private void mockProvidingOtpCode(AuthorisationResponse authorisationResponse) {
        Map<String, String> supplementalInformation = new HashMap<>();
        String authenticationType = getFieldName(authorisationResponse.getChosenScaMethod());
        supplementalInformation.put(authenticationType, OTP_CODE);
        Field tan = GermanFields.Tan.build(catalog, authenticationType, "", null, null);
        when(mockSuppController.askSupplementalInformationSync(argThat(new FieldMatcher(tan))))
                .thenReturn(supplementalInformation);
    }

    private String getFieldName(ScaMethod scaMethod) {
        if (scaMethod != null) {
            String authenticationType = scaMethod.getAuthenticationType();
            if ("CHIP_OTP".equalsIgnoreCase(authenticationType)) {
                return "chipTan";
            } else if ("SMS_OTP".equalsIgnoreCase(authenticationType)) {
                return "smsTan";
            } else if ("PUSH_OTP".equalsIgnoreCase(authenticationType)) {
                return "pushTan";
            }
        }
        return "tanField";
    }

    private void mockScaMethodSelection(
            AuthorisationResponse authorisationResponse, Integer methodNumber) {
        Map<String, String> supplementalInformation = new HashMap<>();
        supplementalInformation.put(CommonFields.Selection.getFieldKey(), methodNumber.toString());
        Field field =
                CommonFields.Selection.build(
                        catalog,
                        authorisationResponse.getScaMethods().stream()
                                .map(ScaMethod::getName)
                                .collect(Collectors.toList()));
        when(mockSuppController.askSupplementalInformationSync(argThat(new FieldMatcher(field))))
                .thenReturn(supplementalInformation);
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
                .thenReturn(getAuthorisationResponse(AUTH_RESP));
        when(requestBuilder.put(any(), any(UpdateAuthorisationRequest.class)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "authorisation_response_updated.json")
                                        .toFile(),
                                AuthorisationResponse.class));

        when(requestBuilder.get(AuthorisationResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "authorisation_response_status.json")
                                        .toFile(),
                                AuthorisationResponse.class));

        when(requestBuilder.get(ConsentDetailsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, consentDetails).toFile(),
                                ConsentDetailsResponse.class));

        when(requestBuilder.get(ConsentStatusResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consent_status_response_valid.json")
                                        .toFile(),
                                ConsentStatusResponse.class));

        return tinkHttpClient;
    }

    private AuthorisationResponse getAuthorisationResponse(String filename) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), AuthorisationResponse.class);
    }

    private static class FieldMatcher implements ArgumentMatcher<Field>, VarargMatcher {

        private final Field left;

        FieldMatcher(Field value) {
            left = value;
        }

        @Override
        public boolean matches(Field argument) {
            return argument != null && left.getDescription().equals(argument.getDescription());
        }
    }
}
