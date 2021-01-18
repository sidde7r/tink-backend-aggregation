package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.xs2a;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aAuthenticationDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class N26Xs2aApiClientTest {

    private N26Xs2aApiClient apiClient;

    @Before
    public void init() {
        TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(
                                LogMaskerImpl.builder().build(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();

        apiClient =
                new N26Xs2aApiClient(
                        httpClient,
                        mock(PersistentStorage.class),
                        mock(Xs2aDevelopersProviderConfiguration.class),
                        true,
                        "USERIP",
                        mock(RandomValueGenerator.class),
                        mock(Xs2aAuthenticationDataAccessor.class));
    }

    @Test
    public void shouldParseXs2aRequest() {
        URL urlInput = URL.of("/berlingroup/v1/token");
        RequestBuilder requestBuilder = apiClient.createRequest(urlInput);

        assertEquals("/v1/berlin-group/v1/token", requestBuilder.getUrl().toString());
    }
}
