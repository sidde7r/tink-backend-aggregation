package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Ignore
public class FetcherTestHelper {
    private static final String OTP_SECRET_KEY =
            "GIYXQ53CGI2TC6DMM5TWK5RVNBTGC6DOORVG253CNRXWI23IOQ3G63TSN5VHEOLMPFWGYNRXNEZDIYTGMJXWUMJTHFUHO6RY";
    private static final String APP_ID = "appId";
    private static final String ACCESS_TOKEN = "accessToken";

    public static PersistentStorage prepareMockedPersistenStorage() {
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        when(persistentStorage.get(Storage.ACCESS_BASIC_TOKEN)).thenReturn(ACCESS_TOKEN);
        when(persistentStorage.get(Storage.ACCESS_DATA_TOKEN)).thenReturn(ACCESS_TOKEN);
        when(persistentStorage.get(Storage.OTP_SECRET_KEY)).thenReturn(OTP_SECRET_KEY);
        when(persistentStorage.get(Storage.APP_ID)).thenReturn(APP_ID);
        return persistentStorage;
    }

    public static RequestBuilder mockRequestBuilder(URL url, TinkHttpClient httpClient) {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(httpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), any())).thenReturn(requestBuilder);
        return requestBuilder;
    }
}
