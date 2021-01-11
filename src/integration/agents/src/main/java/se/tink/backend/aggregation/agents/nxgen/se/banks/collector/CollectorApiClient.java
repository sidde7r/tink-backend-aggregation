package se.tink.backend.aggregation.agents.nxgen.se.banks.collector;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc.TokenExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.identitydata.rpc.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.rpc.SavingsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CollectorApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private AccountListResponse accountList = new AccountListResponse();

    public CollectorApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public InitBankIdResponse initBankid(InitBankIdRequest initBankIdRequest) {
        return client.request(Urls.INIT_BANKID)
                .type(MediaType.APPLICATION_JSON)
                .addBasicAuth(Headers.AUTH_USERNAME, Headers.AUTH_PASSWORD)
                .post(InitBankIdResponse.class, initBankIdRequest);
    }

    public PollBankIdResponse pollBankId(String reference) {
        return client.request(
                        Urls.POLL_BANKID.parameter(CollectorConstants.IdTags.SESSION_ID, reference))
                .type(MediaType.APPLICATION_JSON)
                .addBasicAuth(Headers.AUTH_USERNAME, Headers.AUTH_PASSWORD)
                .get(PollBankIdResponse.class);
    }

    public TokenExchangeResponse exchangeToken() {
        String bearerToken = sessionStorage.get(CollectorConstants.Storage.BEARER_TOKEN);
        return client.request(Urls.TOKEN_EXCHANGE)
                .type(MediaType.APPLICATION_JSON)
                .header(Headers.AUTHORIZATION, bearerToken)
                .post(TokenExchangeResponse.class);
    }

    public AccountListResponse fetchAccounts(String token) {
        if (accountList.isEmpty()) {
            accountList =
                    client.request(Urls.ACCOUNTS)
                            .type(MediaType.APPLICATION_JSON)
                            .header(Headers.AUTHORIZATION, token)
                            .get(AccountListResponse.class);
        }
        return accountList;
    }

    public SavingsResponse getAccountInfo(String accountId) {
        final String bearerToken = sessionStorage.get(CollectorConstants.Storage.BEARER_TOKEN);
        return client.request(
                        Urls.SAVINGS.parameter(CollectorConstants.IdTags.ACCOUNT_ID, accountId))
                .type(MediaType.APPLICATION_JSON)
                .header(Headers.AUTHORIZATION, bearerToken)
                .get(SavingsResponse.class);
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        final String bearerToken = sessionStorage.get(CollectorConstants.Storage.BEARER_TOKEN);
        return client.request(Urls.PROFILE)
                .type(MediaType.APPLICATION_JSON)
                .header(Headers.AUTHORIZATION, bearerToken)
                .get(FetchIdentityDataResponse.class);
    }
}
