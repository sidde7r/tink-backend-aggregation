package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.InitiateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.UrlEncodedFormBody;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.FetchTransactionsRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.FetchAccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class BbvaApiClient {

    private static final int RANDOM_HEX_LENGTH = 64;

    private TinkHttpClient client;
    private String userAgent;
    private String userId;
    private String tsec;

    public BbvaApiClient(TinkHttpClient client) {
        this.client = client;
        this.userAgent = String.format(BbvaConstants.Header.BBVA_USER_AGENT_VALUE,
                generateRandomHex());
    }

    public HttpResponse login(String username, String password) {
        UrlEncodedFormBody loginBody = new UrlEncodedFormBody()
                .add(BbvaConstants.PostParameter.ORIGEN_KEY,
                        BbvaConstants.PostParameter.ORIGEN_VALUE)
                .add(BbvaConstants.PostParameter.EAI_TIPOCP_KEY,
                        BbvaConstants.PostParameter.EAI_TIPOCP_VALUE)
                .add(BbvaConstants.PostParameter.EAI_USER_KEY,
                        BbvaConstants.PostParameter.EAI_USER_VALUE_PREFIX + username)
                .add(BbvaConstants.PostParameter.EAI_PASSWORD_KEY,
                        password);

        return client.request(BbvaConstants.Url.LOGIN)
                .type(BbvaConstants.Header.CONTENT_TYPE_URLENCODED_UTF8)
                .accept(MediaType.WILDCARD)
                .header(BbvaConstants.Header.CONSUMER_ID_KEY, BbvaConstants.Header.CONSUMER_ID_VALUE)
                .header(BbvaConstants.Header.BBVA_USER_AGENT_KEY, userAgent)
                .post(HttpResponse.class, loginBody.getBodyValue());
    }

    public InitiateSessionResponse initiateSession() {
        Map<String, String> body = new HashMap<>();
        body.put(BbvaConstants.PostParameter.CONSUMER_ID_KEY, BbvaConstants.PostParameter.CONSUMER_ID_VALUE);

        HttpResponse response = client.request(BbvaConstants.Url.SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(BbvaConstants.Header.BBVA_USER_AGENT_KEY, userAgent)
                .post(HttpResponse.class, body);

        tsec = response.getHeaders().getFirst(BbvaConstants.Header.TSEC_KEY);

        InitiateSessionResponse initiateSessionResponse = response.getBody(InitiateSessionResponse.class);
        userId = initiateSessionResponse.getUser().getId();

        return initiateSessionResponse;
    }

    public FetchProductsResponse fetchProducts() {

        return createRefererRequest(BbvaConstants.Url.PRODUCTS)
                .get(FetchProductsResponse.class);
    }

    public FetchAccountTransactionsResponse fetchAccountTransactions(Account account, int keyIndex) {
        FetchTransactionsRequestEntity request = createAccountTransactionsQuery(account);

        return createRefererRequest(BbvaConstants.Url.ACCOUNT_TRANSACTION)
                .queryParam(BbvaConstants.Query.PAGINATION_OFFSET, String.valueOf(keyIndex))
                .queryParam(BbvaConstants.Query.PAGE_SIZE, String.valueOf(BbvaConstants.PAGE_SIZE))
                .post(FetchAccountTransactionsResponse.class, request);

    }
    private FetchTransactionsRequestEntity createAccountTransactionsQuery(Account account) {
        FetchTransactionsRequestEntity request = new FetchTransactionsRequestEntity();

        ContractEntity contract = new ContractEntity().setId(account.getAccountNumber());

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
        return createRefererRequest(url)
                .get(String.class);
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
