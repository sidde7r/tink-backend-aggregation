package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.PutConsentsRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ConsentsUtilsTest {

    @Test
    public void shouldPutConsents() {
        // given
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        String accessToken = "accessToken";
        String psuIpAddress = "psuIpAddress";
        TinkHttpClient client = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        List<AccountIdEntity> listOfNecessaryConstents = mock(List.class);
        CreditAgricoleBaseConfiguration creditAgricoleConfiguration =
                mock(CreditAgricoleBaseConfiguration.class);
        String baseUrl = "baseUrl";

        when(oAuth2Token.getAccessToken()).thenReturn(accessToken);
        when(persistentStorage.get(
                        CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));

        when(creditAgricoleConfiguration.getBaseUrl()).thenReturn(baseUrl);
        when(creditAgricoleConfiguration.getPsuIpAddress()).thenReturn(psuIpAddress);

        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.type(anyString())).thenReturn(requestBuilder);
        doNothing().when(requestBuilder).put(ArgumentMatchers.any(PutConsentsRequest.class));

        when(client.request(anyString())).thenReturn(requestBuilder);

        // when
        ConsentsUtils.put(
                persistentStorage, client, listOfNecessaryConstents, creditAgricoleConfiguration);

        // then
        verify(client, times(1)).request(anyString());
        verify(requestBuilder).put(ArgumentMatchers.any(PutConsentsRequest.class));
    }
}
