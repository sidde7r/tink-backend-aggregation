package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Header;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.InitiateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.UrlEncodedFormBody;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.SecurityProfitabilityRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.SecurityProfitabilityResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.ProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaApiClient {
    private static final Logger LOG = LoggerFactory.getLogger(BbvaApiClient.class);

    private TinkHttpClient client;
    private String userAgent;
    private String userId;
    private String tsec;

    public BbvaApiClient(TinkHttpClient client) {
        this.client = client;
        this.userAgent = String.format(Header.BBVA_USER_AGENT_VALUE, BbvaUtils.generateRandomHex());
    }

    private RequestBuilder createRequest(String url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRefererRequest(String url) {
        return createRequest(url)
                .header(Header.ORIGIN_KEY, Header.ORIGIN_VALUE)
                .header(Header.REFERER_KEY, Header.REFERER_VALUE)
                .header(Header.TSEC_KEY, getTsec())
                .header(Header.BBVA_USER_AGENT_KEY, getUserAgent());
    }

    public HttpResponse login(String username, String password) {
        String loginBody =
                UrlEncodedFormBody.createLoginRequest(BbvaUtils.formatUsername(username), password);

        return client.request(BbvaConstants.Url.LOGIN)
                .type(Header.CONTENT_TYPE_URLENCODED_UTF8)
                .accept(MediaType.WILDCARD)
                .header(Header.CONSUMER_ID_KEY, Header.CONSUMER_ID_VALUE)
                .header(Header.BBVA_USER_AGENT_KEY, getUserAgent())
                .post(HttpResponse.class, loginBody);
    }

    public void logout() {
        createRequest(BbvaConstants.Url.SESSION)
                .header(BbvaConstants.Header.BBVA_USER_AGENT_KEY, getUserAgent())
                .delete();
    }

    public ProductsResponse fetchProducts() {
        return createRefererRequest(BbvaConstants.Url.PRODUCTS).get(ProductsResponse.class);
    }

    public AccountTransactionsResponse fetchAccountTransactions(Account account, int keyIndex) {
        TransactionsRequest request = createAccountTransactionsQuery(account);

        return createRefererRequest(BbvaConstants.Url.ACCOUNT_TRANSACTION)
                .queryParam(BbvaConstants.Query.PAGINATION_OFFSET, String.valueOf(keyIndex))
                .queryParam(BbvaConstants.Query.PAGE_SIZE, String.valueOf(BbvaConstants.PAGE_SIZE))
                .post(AccountTransactionsResponse.class, request);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(
            Account account, String keyIndex) {
        return createRefererRequest(BbvaConstants.Url.CREDIT_CARD_TRANSACTIONS)
                .queryParam(
                        BbvaConstants.Query.CONTRACT_ID,
                        account.getFromTemporaryStorage(BbvaConstants.Storage.ACCOUNT_ID))
                .queryParam(
                        BbvaConstants.Query.CARD_TRANSACTION_TYPE,
                        BbvaConstants.AccountType.CREDIT_CARD_SHORT_TYPE)
                .queryParam(BbvaConstants.Query.PAGINATION_OFFSET, keyIndex)
                .get(CreditCardTransactionsResponse.class);
    }

    public SecurityProfitabilityResponse fetchSecurityProfitability(
            String portfolioId, String securityCode) {
        SecurityProfitabilityRequest request =
                SecurityProfitabilityRequest.create(portfolioId, securityCode);

        return createRefererRequest(BbvaConstants.Url.SECURITY_PROFITABILITY)
                .post(SecurityProfitabilityResponse.class, request);
    }

    public TransactionsRequest createAccountTransactionsQuery(Account account) {
        TransactionsRequest request = new TransactionsRequest();

        String accountId = account.getFromTemporaryStorage(BbvaConstants.Storage.ACCOUNT_ID);
        ContractEntity contract = new ContractEntity().setId(accountId);

        AccountContractsEntity accountContract = new AccountContractsEntity();
        accountContract.setContract(contract);

        request.setCustomer(new UserEntity(getUserId()));
        request.setSearchType(BbvaConstants.PostParameter.SEARCH_TYPE);
        request.setAccountContracts(ImmutableList.of(accountContract));

        return request;
    }

    public InitiateSessionResponse initiateSession() throws SessionException, BankServiceException {
        Map<String, String> body = new HashMap<>();
        body.put(
                BbvaConstants.PostParameter.CONSUMER_ID_KEY,
                BbvaConstants.PostParameter.CONSUMER_ID_VALUE);

        HttpResponse response =
                createRequest(BbvaConstants.Url.SESSION)
                        .header(BbvaConstants.Header.BBVA_USER_AGENT_KEY, getUserAgent())
                        .post(HttpResponse.class, body);

        if (MediaType.TEXT_HTML.equalsIgnoreCase(
                response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        setTsec(response.getHeaders().getFirst(BbvaConstants.Header.TSEC_KEY));

        InitiateSessionResponse initiateSessionResponse =
                response.getBody(InitiateSessionResponse.class);

        if (initiateSessionResponse.hasError()) {
            if (initiateSessionResponse.hasError(BbvaConstants.Error.BANK_SERVICE_UNAVAILABLE)) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }

            LOG.warn(
                    String.format(
                            "Bank responded with error: %s",
                            SerializationUtils.serializeToString(
                                    initiateSessionResponse.getResult())));

            throw new IllegalStateException("Failed to initiate session");
        }

        setUserId(initiateSessionResponse.getUser().getId());

        return initiateSessionResponse;
    }

    // LOGGING methods
    public String getLoanDetails(String id) {
        String url =
                new URL(BbvaConstants.Url.LOAN_DETAILS)
                        .parameter(BbvaConstants.Url.PARAM_ID, id)
                        .get();
        return createRefererRequest(url).get(String.class);
    }

    public String getCardTransactions(String id) {
        String url =
                new URL(BbvaConstants.Url.CARD_TRANSACTIONS)
                        .parameter(BbvaConstants.Url.PARAM_ID, id)
                        .get();
        return createRefererRequest(url).get(String.class);
    }

    private String getUserAgent() {
        return userAgent;
    }

    public String getTsec() {
        return tsec;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private void setTsec(String tsec) {
        this.tsec = tsec;
    }
}
