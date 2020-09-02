package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenAuthorization;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenBaseUrl;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenDirectBankingNumber;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenStaticHeaders;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class AccountsCallTest {

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
    private HVBStorage storage = mock(HVBStorage.class);

    private AccountsCall tested = new AccountsCall(httpClient, configurationProvider, storage);

    @Test
    public void prepareRequestShouldReturnHttpRequestForValidArg() {
        // given
        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());
        when(configurationProvider.getStaticHeaders()).thenReturn(givenStaticHeaders());
        when(storage.getAccessToken()).thenReturn(givenAuthorization());

        // when
        HttpRequest results = tested.prepareRequest(givenDirectBankingNumber());

        // then
        assertThat(results.getBody()).isEqualTo(expectedBody());
        assertThat(results.getUrl())
                .isEqualTo(
                        new URL(
                                givenBaseUrl()
                                        + "/adapters/UC_MBX_GL_BE_FACADE_NJ/accounts_fetchAllAccounts"));
        assertThat(results.getMethod()).isEqualTo(POST);
        assertThat(results.getHeaders())
                .isNotEmpty()
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_FORM_URLENCODED)),
                        entry(AUTHORIZATION, singletonList(givenAuthorization())));
    }

    private String expectedBody() {
        return "params=%5B%7B%22platform%22%3A%22iPhone%22%2C%22osVersion%22%3A%2213.5.1%22%2C%22"
                + "appVersion%22%3A%224.2.3%22%2C%22reb%22%3A%22"
                + givenDirectBankingNumber()
                + "%22%7D%5D";
    }

    @Test
    public void parseResponseShouldReturnAccountsResponseForValidResponse() {
        // given
        int givenStatus = 200;
        AccountsResponse givenAccountsResponse = new AccountsResponse();

        HttpResponse givenResponse = mock(HttpResponse.class);
        when(givenResponse.getBody(AccountsResponse.class)).thenReturn(givenAccountsResponse);
        when(givenResponse.getStatus()).thenReturn(givenStatus);

        // when
        ExternalApiCallResult<AccountsResponse> result = tested.parseResponse(givenResponse);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(givenAccountsResponse);
    }
}
