package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CbiGlobeApiClientTest {

    private final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe/resources";

    @Test
    public void empty_transactions_are_not_converted_to_tink_transactions() {
        // given
        TinkHttpClient tinkHttpClient = mockHttpClient();
        CbiGlobeApiClient cbiGlobeApiClient = createCbiGlobeApiClient(tinkHttpClient);

        // when
        GetTransactionsResponse getTransactionsResponse =
                cbiGlobeApiClient.getTransactions(
                        "apiIdentifier", LocalDate.now(), LocalDate.now(), "bookingtType", 1);
        Collection<? extends Transaction> transactions =
                getTransactionsResponse.getTinkTransactions();

        // then
        assertThat(transactions).isEmpty();
    }

    private CbiGlobeApiClient createCbiGlobeApiClient(TinkHttpClient tinkHttpClient) {
        CbiGlobeProviderConfiguration cbiGlobeProviderConfiguration =
                new CbiGlobeProviderConfiguration("aspspCode", "aspspProductCode");
        PersistentStorage persistentStorage = createPersistentStorage();
        return new CbiGlobeApiClient(
                tinkHttpClient,
                persistentStorage,
                new SessionStorage(),
                true,
                new TemporaryStorage(),
                InstrumentType.ACCOUNTS,
                cbiGlobeProviderConfiguration,
                "psuIpAddress");
    }

    private PersistentStorage createPersistentStorage() {
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(StorageKeys.CONSENT_ID, "consentId");
        OAuth2Token oAuth2Token = new OAuth2Token();
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, oAuth2Token);
        return persistentStorage;
    }

    private TinkHttpClient mockHttpClient() {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class))).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.addBearerToken(any(OAuth2Token.class))).thenReturn(requestBuilder);
        when(requestBuilder.queryParam(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(HttpResponse.class)).thenReturn(httpResponse);

        MultivaluedMap<String, String> multivaluedMap = new MultivaluedMapImpl();
        multivaluedMap.putSingle(QueryKeys.TOTAL_PAGES, "1");
        when(httpResponse.getHeaders()).thenReturn(multivaluedMap);

        when(httpResponse.getBody(GetTransactionsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "transactions_response.json").toFile(),
                                GetTransactionsResponse.class));
        return tinkHttpClient;
    }
}
