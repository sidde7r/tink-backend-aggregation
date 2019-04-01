package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import javax.ws.rs.core.MediaType;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.AuthenticateForm;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.*;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public final class LansforsakringarApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public LansforsakringarApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(
                        LansforsakringarConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(LansforsakringarConstants.StorageKeys.CONSENT_ID))
                .header(
                        LansforsakringarConstants.HeaderKeys.PSU_IP_ADDRESS,
                        LansforsakringarConstants.HeaderValues.PSU_IP_ADDRESS)
                .header(
                        LansforsakringarConstants.HeaderKeys.PSU_USER_AGENT,
                        LansforsakringarConstants.HeaderValues.PSU_USER_AGENT)
                .header(LansforsakringarConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID());
    }

    public RequestBuilder createRequestInSession(URL url) {
        OAuth2Token token = getToken().get();
        return createRequest(url)
                .header(
                        LansforsakringarConstants.HeaderKeys.AUTHORIZATION,
                        token.getTokenType() + " " + token.getAccessToken());
    }

    public OAuth2Token authenticate(AuthenticateForm form) {
        return client.request(new URL(LansforsakringarConstants.Urls.AUTHENTICATE))
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(AuthenticateResponse.class)
                .toTinkToken();
    }

    public Collection<TransactionalAccount> getAccounts() {
        return createRequestInSession(
                        new URL(LansforsakringarConstants.Urls.GET_ACCOUNTS)
                                .queryParam(
                                        LansforsakringarConstants.QueryKeys.WITH_BALANCE,
                                        LansforsakringarConstants.QueryValues.TRUE))
                .get(GetAccountsResponse.class)
                .toTinkAccounts();
    }

    private Optional<OAuth2Token> getToken() {
        return sessionStorage.get(
                LansforsakringarConstants.StorageKeys.ACCESS_TOKEN, OAuth2Token.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactions(
            TransactionalAccount account, RequestBuilder req) {
        return req.queryParam(
                        LansforsakringarConstants.QueryKeys.BOOKING_STATUS,
                        LansforsakringarConstants.QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }
}
