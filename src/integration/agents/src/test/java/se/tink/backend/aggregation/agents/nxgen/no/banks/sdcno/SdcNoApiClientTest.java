package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(MockitoJUnitRunner.class)
public class SdcNoApiClientTest {

    private static final String BASE_PAGE_URL = "base/page/url/";
    private static final String BASE_API_URL = "base/api/url/";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RequestBuilder requestBuilder;

    @Mock private TinkHttpClient client;

    @Mock private SdcNoConfiguration agentConfig;

    @InjectMocks private SdcNoApiClient apiClient;

    @Captor private ArgumentCaptor<URL> captor;

    @Before
    public void setUp() {
        given(agentConfig.getBasePageUrl()).willReturn(BASE_PAGE_URL);
        given(agentConfig.getBaseApiUrl()).willReturn(BASE_API_URL);
        given(client.request(any(URL.class))).willReturn(requestBuilder);

        given(requestBuilder.header(anyString(), anyString())).willReturn(requestBuilder);
        given(requestBuilder.accept(MediaType.WILDCARD)).willReturn(requestBuilder);
        given(requestBuilder.type(MediaType.APPLICATION_JSON)).willReturn(requestBuilder);
        given(requestBuilder.overrideHeader(anyString(), anyString())).willReturn(requestBuilder);
    }

    @Test
    public void initWebPageShould() {
        // given
        String initWebPage = "sample init web page";
        given(requestBuilder.get(String.class)).willReturn(initWebPage);

        // when
        String result = apiClient.initWebPage();

        // then
        assertURL(BASE_PAGE_URL + SdcNoConstants.MINE_KONTOER_PATH);

        // and assert response value
        assertThat(result).isEqualTo(initWebPage);
        verify(requestBuilder).get(String.class);

        // and assert headers
        assertStdHeaders();
        verifyNoMoreInteractions(requestBuilder);
    }

    @Test
    public void accountFormPage() {
        // given
        String accountId = "sample-account-id";
        String accountNo = "sample-account-no";
        // and
        String formData =
                "accountno="
                        + accountNo
                        + "&"
                        + "fromaccount=&"
                        + "fromaccountno=&"
                        + "componentkey=&"
                        + "startdate=&"
                        + "enddate=&"
                        + "sortstring=&"
                        + "sortdirection=&"
                        + "sort=&"
                        + "orderby=&"
                        + "selecteddate=&"
                        + "changeAgreementContext=true";

        // when
        apiClient.accountFormPage(accountId, accountNo);

        // then
        assertURL(BASE_PAGE_URL + SdcNoConstants.KONTOBEVEGELSER_PATH + accountId);

        // and assert request
        verify(requestBuilder).post(formData);

        // and assert headers
        assertStdHeaders();
        verify(requestBuilder).overrideHeader("Content-Type", "application/x-www-form-urlencoded");
        verifyNoMoreInteractions(requestBuilder);
    }

    @Test
    public void fetchAgreement() {
        // given
        SdcAgreement sdcAgreement = new SdcAgreement();
        given(requestBuilder.get(SdcAgreement.class)).willReturn(sdcAgreement);

        // when
        SdcAgreement result = apiClient.fetchAgreement();

        // then
        assertURL(BASE_API_URL + SdcNoConstants.USER_AGREEMENT_PATH);

        // and assert response value
        assertThat(result).isEqualTo(sdcAgreement);
        verify(requestBuilder).get(SdcAgreement.class);

        // and assert headers
        assertStdHeaders();
        assertAdditionalHeaders(Headers.API_VERSION_1);
        verifyNoMoreInteractions(requestBuilder);
    }

    @Test
    public void filterAccounts() {
        // given
        FilterAccountsRequest filterRequest = new FilterAccountsRequest();
        FilterAccountsResponse filterResponse = new FilterAccountsResponse();
        // and
        given(requestBuilder.post(FilterAccountsResponse.class, filterRequest))
                .willReturn(filterResponse);

        // when
        FilterAccountsResponse result = apiClient.filterAccounts(filterRequest);

        // then
        assertURL(BASE_API_URL + SdcNoConstants.ACCOUNTS_PATH);

        // and assert response value
        assertThat(result).isEqualTo(filterResponse);
        verify(requestBuilder).post(FilterAccountsResponse.class, filterRequest);

        // and assert headers
        assertStdHeaders();
        assertAdditionalHeaders(Headers.API_VERSION_2);
        verifyNoMoreInteractions(requestBuilder);
    }

    @Test
    public void filterTransactionsFor() {
        // given
        SearchTransactionsRequest transactionsRequest = new SearchTransactionsRequest();
        SearchTransactionsResponse transactionsResponse = new SearchTransactionsResponse();
        // and
        given(requestBuilder.post(SearchTransactionsResponse.class, transactionsRequest))
                .willReturn(transactionsResponse);

        // when
        SearchTransactionsResponse result = apiClient.filterTransactionsFor(transactionsRequest);

        // then
        assertURL(BASE_API_URL + SdcNoConstants.ACCOUNTS_TRANSACTION_PATH);

        // and assert response value
        assertThat(result).isEqualTo(transactionsResponse);
        verify(requestBuilder).post(SearchTransactionsResponse.class, transactionsRequest);

        // and assert headers
        assertStdHeaders();
        assertAdditionalHeaders(Headers.API_VERSION_3);
        verifyNoMoreInteractions(requestBuilder);
    }

    private void assertURL(final String expectedUrl) {
        verify(client).request(captor.capture());
        URL capturedUrl = captor.getValue();
        assertThat(capturedUrl).isEqualTo(new URL(expectedUrl));
    }

    private void assertStdHeaders() {
        verify(requestBuilder).header(Headers.USER_AGENT, Headers.USER_AGENT_VALUE);
        verify(requestBuilder).accept(MediaType.WILDCARD);
        verify(requestBuilder).type(MediaType.APPLICATION_JSON);
    }

    private void assertAdditionalHeaders(final String apiVersion) {
        verify(requestBuilder).header(Headers.X_SDC_API_VERSION, apiVersion);
        verify(requestBuilder).header(Headers.X_SDC_CLIENT_TYPE, Headers.CLIENT_TYPE);
        verify(requestBuilder).header(Headers.X_SDC_LOCALE, Headers.LOCALE_EN);
    }
}
