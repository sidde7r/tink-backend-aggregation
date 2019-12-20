package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class TokenUtilsTest {

    @Test
    public void shouldGetToken() {
        // given
        CreditAgricoleBaseConfiguration creditAgricoleConfiguration =
                mock(CreditAgricoleBaseConfiguration.class);
        String clientId = "clientId";
        String redirectUrl = "redirectUrl";
        String baseUrl = "baseUrl";
        TinkHttpClient client = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        String code = "code";
        TokenResponse tokenResponse = mock(TokenResponse.class);

        when(creditAgricoleConfiguration.getClientId()).thenReturn(clientId);
        when(creditAgricoleConfiguration.getRedirectUrl()).thenReturn(redirectUrl);
        when(creditAgricoleConfiguration.getBaseUrl()).thenReturn(baseUrl);

        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.post(any(), anyString())).thenReturn(tokenResponse);

        when(client.request(anyString())).thenReturn(requestBuilder);

        // when
        TokenResponse resp = TokenUtils.get(creditAgricoleConfiguration, client, code);

        // then
        assertEquals(tokenResponse, resp);
    }

    @Test
    public void shouldRefresh() {
        // given
        CreditAgricoleBaseConfiguration creditAgricoleConfiguration =
                mock(CreditAgricoleBaseConfiguration.class);
        String clientId = "clientId";
        String redirectUrl = "redirectUrl";
        String baseUrl = "baseUrl";
        TinkHttpClient client = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        String refreshToken = "refreshToken";
        TokenResponse tokenResponse = mock(TokenResponse.class);

        when(creditAgricoleConfiguration.getClientId()).thenReturn(clientId);
        when(creditAgricoleConfiguration.getRedirectUrl()).thenReturn(redirectUrl);
        when(creditAgricoleConfiguration.getBaseUrl()).thenReturn(baseUrl);

        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.post(any(), anyString())).thenReturn(tokenResponse);

        when(client.request(anyString())).thenReturn(requestBuilder);

        // when
        TokenResponse resp = TokenUtils.refresh(creditAgricoleConfiguration, client, refreshToken);

        // then
        assertEquals(tokenResponse, resp);
    }
}
