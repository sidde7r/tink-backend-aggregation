package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.http.HttpStatus;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class TransactionsUtilsTest {

    @Test
    public void shouldReturnProperUrlWhenNextKeyIsNull() {
        // given
        String baseUrl = "baseUrl";
        String id = "Id";
        URL nextUrl = null;
        String expectedUrl = "baseUrl/dsp2/v1/accounts/Id/transactions";

        // when
        String url = TransactionsUtils.getUrl(baseUrl, id, nextUrl);

        // then
        assertThat(url, not(isEmptyOrNullString()));
        assertEquals(url, expectedUrl);
    }

    @Test
    public void shouldReturnProperUrlWhenNextKeyIsNotNull() {
        // given
        String baseUrl = "baseUrl";
        String id = null;
        String nextUrlAsString = "/nextUrl/with/some/other/path";
        URL nextUrl = new URL(nextUrlAsString);
        String expectedUrl = "baseUrl/nextUrl/with/some/other/path";

        // when
        String url = TransactionsUtils.getUrl(baseUrl, id, nextUrl);

        // then
        assertThat(url, not(isEmptyOrNullString()));
        assertEquals(expectedUrl, url);
    }

    @Test
    public void shouldReturnProperGetTransactionsResponse() {
        // given
        String baseUrl = "baseUrl";
        String psuIpAddress = "psuIpAddress";
        String id = "id";
        URL next = null;
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        String accessToken = "accessToken";
        TinkHttpClient client = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        CreditAgricoleBaseConfiguration creditAgricoleConfiguration =
                mock(CreditAgricoleBaseConfiguration.class);
        GetTransactionsResponse transactionsResponse = mock(GetTransactionsResponse.class);

        when(oAuth2Token.getAccessToken()).thenReturn(accessToken);
        when(persistentStorage.get(
                        CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));

        when(creditAgricoleConfiguration.getBaseUrl()).thenReturn(baseUrl);
        when(creditAgricoleConfiguration.getPsuIpAddress()).thenReturn(psuIpAddress);

        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.type(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.queryParam(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(HttpResponse.class)).thenReturn(httpResponse);

        when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_OK);
        when(httpResponse.getBody(GetTransactionsResponse.class)).thenReturn(transactionsResponse);

        when(client.request(anyString())).thenReturn(requestBuilder);

        // when
        GetTransactionsResponse resp =
                TransactionsUtils.get(
                        id, next, persistentStorage, client, creditAgricoleConfiguration);

        // then
        assertEquals(transactionsResponse, resp);
    }

    @Test
    public void shouldReturnEmptyGetTransactionsResponseWhenNoContent() {
        // given
        String baseUrl = "baseUrl";
        String psuIpAddress = "psuIpAddress";
        String id = "id";
        URL next = null;
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        String accessToken = "accessToken";
        TinkHttpClient client = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        CreditAgricoleBaseConfiguration creditAgricoleConfiguration =
                mock(CreditAgricoleBaseConfiguration.class);

        when(oAuth2Token.getAccessToken()).thenReturn(accessToken);
        when(persistentStorage.get(
                        CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));

        when(creditAgricoleConfiguration.getBaseUrl()).thenReturn(baseUrl);
        when(creditAgricoleConfiguration.getPsuIpAddress()).thenReturn(psuIpAddress);

        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.type(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.queryParam(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(HttpResponse.class)).thenReturn(httpResponse);

        when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_NO_CONTENT);

        when(client.request(anyString())).thenReturn(requestBuilder);

        // when
        GetTransactionsResponse resp =
                TransactionsUtils.get(
                        id, next, persistentStorage, client, creditAgricoleConfiguration);

        // then
        assertNotNull(resp);
        assertNotNull(resp.getTinkTransactions());
        assertThat(resp.getTinkTransactions().size(), is(0));
    }
}
