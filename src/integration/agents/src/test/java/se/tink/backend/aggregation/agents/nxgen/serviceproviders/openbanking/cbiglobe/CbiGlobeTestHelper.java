package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

@Ignore
public class CbiGlobeTestHelper {

    public static PersistentStorage createPersistentStorage() {
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(StorageKeys.CONSENT_ID, "consentId");
        OAuth2Token oAuth2Token = new OAuth2Token();
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, oAuth2Token);
        return persistentStorage;
    }

    public static TinkHttpClient mockHttpClient(
            HttpResponse httpResponse, RequestBuilder requestBuilder) {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);

        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class))).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.addBearerToken(any(OAuth2Token.class))).thenReturn(requestBuilder);
        when(requestBuilder.queryParam(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(HttpResponse.class)).thenReturn(httpResponse);
        return tinkHttpClient;
    }

    public static CbiGlobeApiClient createCbiGlobeApiClient(TinkHttpClient tinkHttpClient) {
        CbiGlobeProviderConfiguration cbiGlobeProviderConfiguration =
                new CbiGlobeProviderConfiguration("aspspCode", "aspspProductCode");
        PersistentStorage persistentStorage = createPersistentStorage();
        return new CbiGlobeApiClient(
                tinkHttpClient,
                persistentStorage,
                new SessionStorage(),
                new TemporaryStorage(),
                InstrumentType.ACCOUNTS,
                cbiGlobeProviderConfiguration,
                "psuIpAddress");
    }
}
