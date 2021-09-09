package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount;

import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ProductionUrls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IcaBankenTransactionalAccountFetcherTest {
    private PersistentStorage persistentStorage;
    private TinkHttpClient client;
    IcaBankenTransactionalAccountFetcher icaBankenTransactionalAccountFetcher;

    @Before
    public void setUp() {
        persistentStorage = mock(PersistentStorage.class);
        client = mock(TinkHttpClient.class);
        IcaBankenApiClient apiClient = new IcaBankenApiClient(client, persistentStorage);
        icaBankenTransactionalAccountFetcher = new IcaBankenTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldReturnEmptyListWhenBankSendsEmptyAccountResponse() {
        // given
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);

        // when
        Mockito.when(client.request(new URL(ProductionUrls.ACCOUNTS_PATH)))
                .thenReturn(requestBuilder);
        Mockito.when(requestBuilder.queryParam(Mockito.any(), Mockito.any()))
                .thenReturn(requestBuilder);
        Mockito.when(requestBuilder.header(Mockito.any(), Mockito.any()))
                .thenReturn(requestBuilder, requestBuilder);
        Mockito.when(requestBuilder.addBearerToken(Mockito.any())).thenReturn(requestBuilder);
        Mockito.when(persistentStorage.get(StorageKeys.TOKEN, OAuth2Token.class))
                .thenReturn(Optional.ofNullable(oAuth2Token));
        Mockito.when(requestBuilder.get(FetchAccountsResponse.class)).thenReturn(getAccounts());

        // then
        Assert.assertEquals(
                Collections.emptyList(), icaBankenTransactionalAccountFetcher.fetchAccounts());
    }

    private FetchAccountsResponse getAccounts() {
        return SerializationUtils.deserializeFromString(
                "{\"accounts\": []}", FetchAccountsResponse.class);
    }
}
