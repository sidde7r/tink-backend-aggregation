package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.BPostApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BPostAuthenticatorTest {

    private static final String GIVEN_STATE = "SOME_STATE";
    private BPostAuthenticator objectUnderTest;
    private Xs2aDevelopersApiClient xs2aDevelopersApiClient;
    private BPostApiClient bPostApiClient;
    private LocalDateTimeSource localDateTimeSource;
    private Xs2aDevelopersProviderConfiguration xs2aDevelopersProviderConfiguration;

    @Before
    public void setup() {
        PersistentStorage persistentStorage = new PersistentStorage();
        RandomValueGenerator randomValueGenerator = mock(RandomValueGenerator.class);
        xs2aDevelopersProviderConfiguration = mock(Xs2aDevelopersProviderConfiguration.class);
        Xs2aDevelopersApiClient realXs2aDevelopersApiClient =
                new Xs2aDevelopersApiClient(
                        null,
                        persistentStorage,
                        xs2aDevelopersProviderConfiguration,
                        true,
                        null,
                        randomValueGenerator);
        xs2aDevelopersApiClient = spy(realXs2aDevelopersApiClient);
        localDateTimeSource = mock(LocalDateTimeSource.class);
        bPostApiClient = mock(BPostApiClient.class);
        objectUnderTest =
                new BPostAuthenticator(
                        xs2aDevelopersApiClient,
                        new PersistentStorage(),
                        null,
                        localDateTimeSource,
                        new Credentials(),
                        bPostApiClient);
    }

    @Test
    public void shouldBuildCorrectAuthorizeUrl() {
        initMocks();
        URL result = objectUnderTest.buildAuthorizeUrl(GIVEN_STATE);
        assertThat(result).isNotNull();
        // code challenge is generated each time and there is no really good way to deal with it
        String cleanedUrl =
                result.toString()
                        .replaceAll("code_challenge=.+?&", "code_challenge=someCodeChallenge&");
        assertThat(cleanedUrl).isEqualTo(BPostAuthenticatorTestFixtures.expectedCleanedUrl);
    }

    private void initMocks() {
        when(xs2aDevelopersProviderConfiguration.getBaseUrl()).thenReturn("https://base.url");
        when(xs2aDevelopersProviderConfiguration.getRedirectUrl())
                .thenReturn("https://redirect.url");
        doReturn(BPostAuthenticatorTestFixtures.givenConsentResponse())
                .when(xs2aDevelopersApiClient)
                .createConsent(any());
        when(bPostApiClient.getAuthorizationEndpoint(eq("https://url.for.authorization.endpoint")))
                .thenReturn("https://the.actual.authorization.endpoint");
        when(localDateTimeSource.now()).thenReturn(LocalDateTime.of(2020, 1, 10, 0, 0));
    }
}
