package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import lombok.SneakyThrows;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.VarargMatcher;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbUserIpInformation;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.configuration.DkbConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DkbAuthenticatorTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/dkb/resources/";

    @Test
    @SneakyThrows
    public void when_user_is_authenticated_then_session_expiry_date_is_set() {
        // given
        Date date = ThreadSafeDateFormat.FORMATTER_DAILY_DEFAULT_TIMEZONE.parse("2030-01-01");
        TinkHttpClient tinkHttpClient = mockHttpClient();
        Credentials credentials = createCredentials(null);
        DkbAuthenticator dkbAuthenticator =
                createDkbAuthenticator(
                        tinkHttpClient, credentials, new DkbStorage(new PersistentStorage()));

        // when
        dkbAuthenticator.authenticate(credentials);

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualToIgnoringHours(date);
    }

    static TinkHttpClient mockHttpClient() {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);

        mockRequest(
                tinkHttpClient,
                HttpMethod.POST,
                "https://api.dkb.de/oauth2/token",
                null,
                Wso2Token.class,
                "oauth2_token_response.json");
        mockRequest(
                tinkHttpClient,
                HttpMethod.POST,
                "https://api.dkb.de/pre-auth/psd2-auth/v1/auth/token",
                null,
                AuthResult.class,
                "auth_token_response.json");
        mockRequest(
                tinkHttpClient,
                HttpMethod.POST,
                "https://api.dkb.de/psd2/v1/consents",
                null,
                ConsentResponse.class,
                "consent_new.json");

        mockRequest(
                tinkHttpClient,
                HttpMethod.GET,
                "https://api.dkb.de/psd2/v1/consents/consentId",
                null,
                ConsentDetailsResponse.class,
                "consent_existing.json");

        mockRequest(
                tinkHttpClient,
                HttpMethod.POST,
                "https://api.dkb.de/psd2/v1/consents/consentId/authorisations",
                null,
                ConsentAuthorization.class,
                "consent_authorisation.json");

        DkbAuthRequestsFactory.ConsentAuthorizationMethod consentAuthorizationMethod =
                new DkbAuthRequestsFactory.ConsentAuthorizationMethod("authenticationMethodId");
        mockRequest(
                tinkHttpClient,
                HttpMethod.PUT,
                "https://api.dkb.de/psd2/v1/consents/consentId/authorisations/authorisationId",
                consentAuthorizationMethod,
                ConsentAuthorization.class,
                "consent_authorisation_selected.json");

        DkbAuthRequestsFactory.ConsentAuthorizationOtp consentAuthorizationOtp =
                new DkbAuthRequestsFactory.ConsentAuthorizationOtp("code");
        mockRequest(
                tinkHttpClient,
                HttpMethod.PUT,
                "https://api.dkb.de/psd2/v1/consents/consentId/authorisations/authorisationId",
                consentAuthorizationOtp,
                ConsentAuthorization.class,
                "consent_authorisation_finalised.json");

        return tinkHttpClient;
    }

    static DkbStorage createDkbStorage() {
        DkbStorage dkbStorage = new DkbStorage(new PersistentStorage());
        dkbStorage.setWso2OAuthToken(
                OAuth2Token.create("refreshToken", "accessToken", "refreshToken", 1000));
        dkbStorage.setAccessToken(OAuth2Token.create("accessToken", "accessToken", null, 100));
        dkbStorage.setConsentId("consentId");
        return dkbStorage;
    }

    static Credentials createCredentials(Date sessionExpiryDate) {
        Credentials credentials = new Credentials();
        credentials.setUsername("username");
        credentials.setPassword("password");
        credentials.setSessionExpiryDate(sessionExpiryDate);
        return credentials;
    }

    static DkbAuthenticator createDkbAuthenticator(
            TinkHttpClient tinkHttpClient, Credentials credentials, DkbStorage dkbStorage) {
        DkbConfiguration dkbConfiguration = new DkbConfiguration();
        dkbConfiguration.setConsumerId("consumerId");
        dkbConfiguration.setConsumerSecret("consumerSecret");
        dkbConfiguration.setClientId("clientId");
        dkbConfiguration.setClientSecret("clientSecret");

        DkbUserIpInformation dkbUserIpInformation = new DkbUserIpInformation(true, "1.1.1.1");
        DkbAuthRequestsFactory dkbAuthRequestsFactory =
                new DkbAuthRequestsFactory(dkbConfiguration, dkbStorage, dkbUserIpInformation);

        SupplementalInformationHelper supplementalInformationHelper =
                createSupplementalInformationHelper();

        DkbAuthApiClient dkbAuthApiClient =
                new DkbAuthApiClient(tinkHttpClient, dkbAuthRequestsFactory, dkbStorage);
        Catalog catalog = Catalog.getCatalog("DE_de");
        DkbSupplementalDataProvider dkbSupplementalDataProvider =
                new DkbSupplementalDataProvider(supplementalInformationHelper, catalog);

        return new DkbAuthenticator(
                dkbAuthApiClient, dkbSupplementalDataProvider, dkbStorage, credentials);
    }

    private static SupplementalInformationHelper createSupplementalInformationHelper() {
        SupplementalInformationHelper supplementalInformationHelper =
                mock(SupplementalInformationHelper.class);
        when(supplementalInformationHelper.askSupplementalInformation(any(Field.class)))
                .thenReturn(Collections.singletonMap("tanField", "code"));
        return supplementalInformationHelper;
    }

    static void mockRequest(
            TinkHttpClient tinkHttpClient,
            HttpMethod method,
            String url,
            Object body,
            Class clazz,
            String fileName) {
        HttpRequest request = new HttpRequestImpl(method, new URL(url), body);
        HttpResponse response = mock(HttpResponse.class);

        when(tinkHttpClient.request(
                        (Class<HttpResponse>) any(Class.class),
                        argThat(new HttpRequestMatcher(request))))
                .thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody(clazz))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, fileName).toFile(), clazz));
    }

    private static class HttpRequestMatcher implements ArgumentMatcher<HttpRequest>, VarargMatcher {

        private final HttpRequest left;

        HttpRequestMatcher(HttpRequest value) {
            left = value;
        }

        @Override
        public boolean matches(HttpRequest argument) {
            boolean bodyEquals = true;
            if (left.getBody() != null && argument.getBody() != null) {
                bodyEquals = left.getBody().equals(argument.getBody());
            }
            return argument != null
                    && ((left.getUrl().equals(argument.getUrl())
                                    && left.getMethod().equals(argument.getMethod()))
                            && bodyEquals);
        }
    }
}
