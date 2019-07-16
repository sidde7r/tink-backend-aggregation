package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup;

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class BpceGroupApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private BpceGroupConfiguration configuration;

    public BpceGroupApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private BpceGroupConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(BpceGroupConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        String accessToken =
                persistentStorage
                        .get(StorageKeys.ACCESS_TOKEN, String.class)
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.TOKENT_NOT_FOUND));

        return createRequest(url)
                .header(HeaderKeys.AUTHORIZATION, HeaderValues.TOKEN_PREFIX + accessToken)
                .header(HeaderKeys.SIGNATURE, HeaderValues.EMPTY)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString());
    }

    public TokenResponse authenticate() {

        TokenRequest tokenRequest =
                new TokenRequest(
                        FormValues.SCOPE,
                        FormValues.GRANT_TYPE,
                        FormValues.CDETAB,
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret());

        return client.request(new URL(Urls.BASE_AUTH_URL))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(QueryKeys.SBX_ROID, QueryValues.SBX_ROID)
                .queryParam(QueryKeys.SBX_CDETAB, QueryValues.SBX_CDETAB)
                .queryParam(QueryKeys.SBX_TPPID, QueryValues.SBX_TPPID)
                .post(TokenResponse.class, tokenRequest.toData());
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(Urls.ACCOUNTS).get(AccountsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactions(TransactionalAccount account) {
        return createRequestInSession(
                        Urls.TRANSACTIONS.parameter(
                                UrlParameters.ACCOUNT_ID, account.getApiIdentifier()))
                .get(TransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactions(String key) {
        return createRequestInSession(new URL(key)).get(TransactionsResponse.class);
    }
}
