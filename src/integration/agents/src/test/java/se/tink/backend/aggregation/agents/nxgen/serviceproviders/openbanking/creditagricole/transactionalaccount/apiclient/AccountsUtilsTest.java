package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class AccountsUtilsTest {

    @Test
    public void shouldGetAccounts() {
        // given
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        String accessToken = "accessToken";
        String psuIpAddress = "psuIpAddress";
        TinkHttpClient client = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        CreditAgricoleBaseConfiguration creditAgricoleConfiguration =
                mock(CreditAgricoleBaseConfiguration.class);
        GetAccountsResponse getAccountsResponse = mock(GetAccountsResponse.class);

        when(creditAgricoleConfiguration.getPsuIpAddress()).thenReturn(psuIpAddress);

        when(oAuth2Token.getAccessToken()).thenReturn(accessToken);
        when(persistentStorage.get(
                        CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));

        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.type(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(GetAccountsResponse.class)).thenReturn(getAccountsResponse);

        when(client.request(anyString())).thenReturn(requestBuilder);

        // when
        GetAccountsResponse response =
                AccountsUtils.get(persistentStorage, client, creditAgricoleConfiguration);

        // then
        assertEquals(getAccountsResponse, response);
    }
}
