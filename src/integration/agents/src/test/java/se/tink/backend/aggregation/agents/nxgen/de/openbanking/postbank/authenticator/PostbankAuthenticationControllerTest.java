package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
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
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
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

    @Test
    public void when_user_is_authenticated_then_session_expiry_date_is_set() {
        // given
        Date date = toDate("2030-01-01");
        TinkHttpClient tinkHttpClient = mockHttpClient(CONSENT_DETAILS_VALID);
        Credentials credentials = createCredentials(null);
        credentials.setType(CredentialsTypes.PASSWORD);

        PostbankAuthenticator postbankAuthenticator =
                createPostbankAuthenticator(tinkHttpClient, new PersistentStorage(), credentials);

        PostbankAuthenticationController postbankAuthenticationController =
                createAutoAuthenticationController(postbankAuthenticator, credentials, true);

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
        credentials.setUsername("username");
        credentials.setPassword("password");
        credentials.setSessionExpiryDate(sessionExpiryDate);
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
            PostbankAuthenticator postbankAuthenticator, Credentials credentials, boolean manual) {
        AuthorisationResponse authorisationResponse = getAuthorisationResponse();
        Catalog catalog = new Catalog(Locale.getDefault());

        SupplementalInformationHelper supplementalInformationHelper =
                mock(SupplementalInformationHelper.class);
        mockScaMethodSelection(authorisationResponse, catalog, supplementalInformationHelper, 3);
        mockProvidingOtpCode(authorisationResponse, catalog, supplementalInformationHelper, 3);

        return new PostbankAuthenticationController(
                catalog,
                supplementalInformationHelper,
                mock(SupplementalRequester.class),
                postbankAuthenticator);
    }

    private void mockProvidingOtpCode(
            AuthorisationResponse authorisationResponse,
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            Integer methodNumber) {
        Map<String, String> supplementalInformation = new HashMap<>();
        supplementalInformation.put(CommonFields.Selection.getFieldKey(), "otpCode");
        Field tan =
                GermanFields.Tan.build(
                        catalog, authorisationResponse.getScaMethods().get(methodNumber).getName());
        when(supplementalInformationHelper.askSupplementalInformation(
                        argThat(new FieldMatcher(tan))))
                .thenReturn(supplementalInformation);
    }

    private void mockScaMethodSelection(
            AuthorisationResponse authorisationResponse,
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            Integer methodNumber) {
        Map<String, String> supplementalInformation = new HashMap<>();
        supplementalInformation.put(CommonFields.Selection.getFieldKey(), methodNumber.toString());
        Field field =
                CommonFields.Selection.build(
                        catalog,
                        authorisationResponse.getScaMethods().stream()
                                .map(ScaMethod::getName)
                                .collect(Collectors.toList()));
        when(supplementalInformationHelper.askSupplementalInformation(
                        argThat(new FieldMatcher(field))))
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
        when(requestBuilder.put(any(), anyString())).thenReturn(getAuthorisationResponse());
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

    private AuthorisationResponse getAuthorisationResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "authorisation_response.json").toFile(),
                AuthorisationResponse.class);
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
