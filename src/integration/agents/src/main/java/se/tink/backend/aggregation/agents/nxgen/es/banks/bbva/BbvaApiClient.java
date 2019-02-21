package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.InitiateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.UrlEncodedFormBody;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.SecurityProfitabilityRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.SecurityProfitabilityResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.FetchTransactionsRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.FetchAccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaApiClient {
    private static final Logger LOG = LoggerFactory.getLogger(BbvaApiClient.class);

    private static final int RANDOM_HEX_LENGTH = 64;
    private static final Pattern NIE_PATTERN = Pattern.compile("(?i)^[XY].+[A-Z]$");

    private TinkHttpClient client;
    private String userAgent;
    private String userId;
    private String tsec;

    public BbvaApiClient(TinkHttpClient client) {
        this.client = client;
        this.userAgent = String.format(BbvaConstants.Header.BBVA_USER_AGENT_VALUE, generateRandomHex());
    }

    // Non NIE usernames must be prepended with '0' (based on ambassador credentials) while NIE
    // usernames are passed along as-is.
    private static String formatUsername(String username) {
        if (NIE_PATTERN.matcher(username).matches()) {
            return username;
        }

        return String.format("0%s", username);
    }

    public HttpResponse login(String username, String password) {
        String loginBody = UrlEncodedFormBody.createLoginRequest(formatUsername(username), password);

        return client.request(BbvaConstants.Url.LOGIN)
                .type(BbvaConstants.Header.CONTENT_TYPE_URLENCODED_UTF8)
                .accept(MediaType.WILDCARD)
                .header(BbvaConstants.Header.CONSUMER_ID_KEY, BbvaConstants.Header.CONSUMER_ID_VALUE)
                .header(BbvaConstants.Header.BBVA_USER_AGENT_KEY, userAgent)
                .post(HttpResponse.class, loginBody);
    }

    public InitiateSessionResponse initiateSession() throws SessionException, BankServiceException {
        Map<String, String> body = new HashMap<>();
        body.put(BbvaConstants.PostParameter.CONSUMER_ID_KEY, BbvaConstants.PostParameter.CONSUMER_ID_VALUE);

        HttpResponse response = client.request(BbvaConstants.Url.SESSION)
                        .type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(BbvaConstants.Header.BBVA_USER_AGENT_KEY, userAgent)
                        .post(HttpResponse.class, body);

        if (MediaType.TEXT_HTML.equalsIgnoreCase(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        tsec = response.getHeaders().getFirst(BbvaConstants.Header.TSEC_KEY);

        InitiateSessionResponse initiateSessionResponse = response.getBody(InitiateSessionResponse.class);

        if (initiateSessionResponse.hasError()) {
            if (initiateSessionResponse.hasError(BbvaConstants.Error.BANK_SERVICE_UNAVAILABLE)) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }

            LOG.warn(
                    String.format(
                            "Bank responded with error: %s",
                            SerializationUtils.serializeToString(initiateSessionResponse.getResult()))
            );

            throw new IllegalStateException("Failed to initiate session");
        }

        userId = initiateSessionResponse.getUser().getId();

        return initiateSessionResponse;
    }

    public FetchProductsResponse fetchProducts() {

        return createRefererRequest(BbvaConstants.Url.PRODUCTS).get(FetchProductsResponse.class);
    }

    public FetchAccountTransactionsResponse fetchAccountTransactions(
            Account account, int keyIndex) {
        FetchTransactionsRequestEntity request = createAccountTransactionsQuery(account);

        return createRefererRequest(BbvaConstants.Url.ACCOUNT_TRANSACTION)
                .queryParam(BbvaConstants.Query.PAGINATION_OFFSET, String.valueOf(keyIndex))
                .queryParam(BbvaConstants.Query.PAGE_SIZE, String.valueOf(BbvaConstants.PAGE_SIZE))
                .post(FetchAccountTransactionsResponse.class, request);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(Account account, String keyIndex) {
        return createRefererRequest(BbvaConstants.Url.CREDIT_CARD_TRANSACTIONS)
                .queryParam(
                        BbvaConstants.Query.CONTRACT_ID,
                        account.getFromTemporaryStorage(BbvaConstants.Storage.ACCOUNT_ID))
                .queryParam(BbvaConstants.Query.CARD_TRANSACTION_TYPE, BbvaConstants.AccountTypes.CREDIT_CARD_SHORT_TYPE)
                .queryParam(BbvaConstants.Query.PAGINATION_OFFSET, keyIndex)
                .get(CreditCardTransactionsResponse.class);
    }

    public SecurityProfitabilityResponse fetchSecurityProfitability(String portfolioId, String securityCode) {
        SecurityProfitabilityRequest request = SecurityProfitabilityRequest.create(portfolioId, securityCode);

        return createRefererRequest(BbvaConstants.Url.SECURITY_PROFITABILITY)
                .post(SecurityProfitabilityResponse.class, request);
    }

    private FetchTransactionsRequestEntity createAccountTransactionsQuery(Account account) {
        FetchTransactionsRequestEntity request = new FetchTransactionsRequestEntity();

        String accountId = account.getFromTemporaryStorage(BbvaConstants.Storage.ACCOUNT_ID);
        ContractEntity contract = new ContractEntity().setId(accountId);

        AccountContractsEntity accountContract = new AccountContractsEntity();
        accountContract.setContract(contract);

        request.setCustomer(new UserEntity(userId));
        request.setSearchType(BbvaConstants.PostParameter.SEARCH_TYPE);
        request.setAccountContracts(ImmutableList.of(accountContract));

        return request;
    }

    public void logout() {

        client.request(BbvaConstants.Url.SESSION)
                .type(MediaType.APPLICATION_JSON)
                .header(BbvaConstants.Header.BBVA_USER_AGENT_KEY, userAgent)
                .accept(MediaType.APPLICATION_JSON)
                .delete();
    }

    // LOGGING methods
    public String getLoanDetails(String id) {
        String url = new URL(BbvaConstants.Url.LOAN_DETAILS).parameter(BbvaConstants.Url.PARAM_ID, id).get();
        return createRefererRequest(url).get(String.class);
    }

    public String getCardTransactions(String id) {
        String url = new URL(BbvaConstants.Url.CARD_TRANSACTIONS).parameter(BbvaConstants.Url.PARAM_ID, id).get();
        return createRefererRequest(url)
                .get(String.class);
    }

    private RequestBuilder createRefererRequest(String url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(BbvaConstants.Header.ORIGIN_KEY, BbvaConstants.Header.ORIGIN_VALUE)
                .header(BbvaConstants.Header.REFERER_KEY, BbvaConstants.Header.REFERER_VALUE)
                .header(BbvaConstants.Header.TSEC_KEY, tsec)
                .header(BbvaConstants.Header.BBVA_USER_AGENT_KEY, userAgent);
    }

    private String generateRandomHex() {
        Random random = new Random();
        byte[] randBytes = new byte[RANDOM_HEX_LENGTH];
        random.nextBytes(randBytes);

        return Hex.encodeHexString(randBytes).toUpperCase();
    }
}
