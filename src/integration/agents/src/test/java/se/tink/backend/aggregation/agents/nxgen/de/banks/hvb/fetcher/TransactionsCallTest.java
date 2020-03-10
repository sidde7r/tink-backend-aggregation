package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static java.time.LocalDate.parse;
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
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenBranchId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenDirectBankingNumber;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenStaticHeaders;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsCall.Arg;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class TransactionsCallTest {

    private static final String GIVEN_ACCOUNT_NUMBER = "87654321";
    private static final LocalDate GIVEN_DATE_TO = parse("2020-01-02");
    private static final LocalDate GIVEN_DATE_FROM = parse("2019-01-02");

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
    private HVBStorage storage = mock(HVBStorage.class);

    private TransactionsCall tested =
            new TransactionsCall(httpClient, configurationProvider, storage);

    @Test
    public void prepareRequestShouldReturnHttpRequestForValidArg() {
        // given
        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());
        when(configurationProvider.getStaticHeaders()).thenReturn(givenStaticHeaders());
        when(storage.getAccessToken()).thenReturn(givenAuthorization());

        Arg givenArg =
                Arg.builder()
                        .directBankingNumber(givenDirectBankingNumber())
                        .branchNumber(givenBranchId())
                        .accountNumber(GIVEN_ACCOUNT_NUMBER)
                        .dateFrom(GIVEN_DATE_FROM)
                        .dateTo(GIVEN_DATE_TO)
                        .build();

        // when
        HttpRequest results = tested.prepareRequest(givenArg);

        // then
        assertThat(results.getBody()).isEqualTo(expectedBody());
        assertThat(results.getUrl())
                .isEqualTo(
                        new URL(
                                givenBaseUrl()
                                        + "/adapters/UC_MBX_GL_BE_FACADE_NJ/accounts_requestCurrentAccountTransactionList"));
        assertThat(results.getMethod()).isEqualTo(POST);
        assertThat(results.getHeaders())
                .isNotEmpty()
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_FORM_URLENCODED)),
                        entry(AUTHORIZATION, singletonList(givenAuthorization())));
    }

    private String expectedBody() {
        return "params=%5B%7B%22accountNumber%22%3A%22"
                + GIVEN_ACCOUNT_NUMBER
                + "%22%2C%22branchNumber%22%3A%22"
                + givenBranchId()
                + "%22%2C%22bookingDateFrom%22%3A%22"
                + GIVEN_DATE_FROM.toString()
                + "%22%2C%22bookingDateTo%22%3A%22"
                + GIVEN_DATE_TO.toString()
                + "%22%2C%22correlationId%22%3A%22test1%22%2C%22callCounter%22%3A0%2C%22reb%22%3A%22"
                + givenDirectBankingNumber()
                + "%22%2C%22isSearch%22%3Afalse%7D%5D";
    }

    @Test
    public void parseResponseShouldReturnTransactionsResponseForValidResponse() {
        // given
        int givenStatus = 200;
        TransactionsResponse givenTransactionsResponse = new TransactionsResponse();

        HttpResponse givenResponse = mock(HttpResponse.class);
        when(givenResponse.getBody(TransactionsResponse.class))
                .thenReturn(givenTransactionsResponse);
        when(givenResponse.getStatus()).thenReturn(givenStatus);

        // when
        ExternalApiCallResult<TransactionsResponse> result = tested.parseResponse(givenResponse);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(givenTransactionsResponse);
    }
}
