package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants.HeaderValue;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants.QueryKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants.QueryValue;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc.BankIdCollectRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc.UserResponse;
import se.tink.backend.aggregation.agents.utils.jsoup.ElementUtils;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class EnterCardApiClient {

    private final TinkHttpClient client;
    private final EnterCardConfiguration config;

    EnterCardApiClient(TinkHttpClient client, EnterCardConfiguration config) {
        this.client = client;
        this.config = config;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public String fetchBankIdInitPage() {
        return createRequest(getInitRequest()).get(String.class);
    }

    public BankIdInitResponse orderBankId(String signicatUrl, String ssn) {
        return createRequest(new URL(signicatUrl + Urls.ORDER))
                .post(BankIdInitResponse.class, new BankIdInitRequest(ssn));
    }

    public BankIdCollectResponse collectBankId(URL collectUrl, BankIdCollectRequest request) {
        return createRequest(collectUrl).post(BankIdCollectResponse.class, request);
    }

    public String completeBankId(URL completeUrl, BankIdCollectRequest collectRequest) {
        return client.request(completeUrl)
                .header(EnterCardConstants.REFERER, getInitRequest().toString())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(String.class, collectRequest);
    }

    public void roundTripTest(String url) {
        client.request(url).get(String.class);
    }

    public UserResponse fetchUserDetails() {
        return client.request(config.getUserUrl())
                .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderValue.ACCEPT_LANGUAGE)
                .accept(config.getJsonVendorMime())
                .get(UserResponse.class);
    }

    public AccountResponse fetchCardAccount(String accountIdentifier) {
        return client.request(config.getAccountUrl(accountIdentifier))
                .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderValue.ACCEPT_LANGUAGE)
                .accept(config.getJsonVendorMime())
                .get(AccountResponse.class);
    }

    public TransactionResponse fetchTransactions(String accountNumber, int page, int perPage) {
        return client.request(config.getTransactionsUrl(accountNumber))
                .queryParam(QueryKey.PAGE, "" + page)
                .queryParam(QueryKey.PER_PAGE, "" + perPage)
                .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderValue.ACCEPT_LANGUAGE)
                .accept(config.getJsonVendorMime())
                .get(TransactionResponse.class);
    }

    private URL getInitRequest() {
        return new URL(Urls.BANK_ID_INIT)
                .queryParam(QueryKey.ID, config.getSignicatId())
                .queryParam(QueryKey.TARGET, config.getAuthUrl())
                .queryParam(QueryKey.PREFILLED_MODE, QueryValue.PREFILLED_MODE);
    }

    public String auth(Element formElement) {
        return client.request(formElement.attr("action"))
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(EnterCardConstants.REFERER, getInitRequest().toString())
                .header("Origin", "https://id.signicat.com")
                .post(String.class, ElementUtils.parseFormParameters(formElement));
    }
}
