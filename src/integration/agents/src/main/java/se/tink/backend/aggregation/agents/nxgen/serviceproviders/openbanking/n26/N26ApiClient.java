package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Header;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.UrlParam;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.storage.N26Storage;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class N26ApiClient {

    private static final String PAGE_LIMIT = "page.limit";
    private static final String PAGE_OFFSET = "page.offset";
    private static final String DEFAULT_PAGE_LIMIT = "50";

    private final TinkHttpClient client;
    private final AgentConfiguration<N26Configuration> configuration;
    private final N26Storage storage;

    public TokenResponse tokenRequest(TokenRequest request) {
        return createRequest(createUrl(Url.TOKEN_REQUEST)).post(TokenResponse.class, request);
    }

    public TokenDetailsResponse tokenDetails(String tokenId) {
        final URL url = createUrl(Url.TOKEN_INFO).parameter(UrlParam.TOKEN_ID, tokenId);
        return createRequest(url).get(TokenDetailsResponse.class);
    }

    public AccountsResponse getAccounts() {
        return createRequestWithToken(createUrl(Url.ACCOUNTS)).get(AccountsResponse.class);
    }

    public AccountBalanceResponse getAccountBalance(String accountId) {
        final URL url = createUrl(Url.ACCOUNT_BALANCE).parameter(UrlParam.ACCOUNT_ID, accountId);
        return createRequestWithToken(url).get(AccountBalanceResponse.class);
    }

    public AccountTransactionsResponse getAccountTransactions(String accountId, String offset) {
        URL url =
                createUrl(Url.ACCOUNT_TRANSACTIONS)
                        .parameter(UrlParam.ACCOUNT_ID, accountId)
                        .queryParam(PAGE_LIMIT, DEFAULT_PAGE_LIMIT)
                        .queryParam(PAGE_OFFSET, offset);

        return createRequestWithToken(url).get(AccountTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        return createRequest(createUrl(Url.TRANSFERS))
                .post(CreatePaymentResponse.class, SerializationUtils.serializeToString(request));
    }

    public GetPaymentResponse getPayment(String transferId) {
        final URL url = createUrl(Url.TRANSFER_DETAILS).parameter(UrlParam.TRANSFER_ID, transferId);
        return createRequest(url).get(GetPaymentResponse.class);
    }

    private RequestBuilder createRequestWithToken(URL url) {
        return createRequest(url).header(Header.ON_BEHALF_OF, storage.getAccessToken());
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(Header.AUTHORIZATION, getAuthorizationHeaderValue())
                .header(Header.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    private String getAuthorizationHeaderValue() {
        return Header.BASIC + configuration.getProviderSpecificConfiguration().getApiKey();
    }

    private URL createUrl(String path) {
        return URL.of(configuration.getProviderSpecificConfiguration().getBaseUrl() + path);
    }
}
