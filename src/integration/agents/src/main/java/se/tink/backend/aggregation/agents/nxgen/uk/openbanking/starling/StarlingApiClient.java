package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.UrlParams;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderNameResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.BusinessAccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.SoleTraderAccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.StarlingAccountHolderType;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.Token;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class StarlingApiClient {

    private final TinkHttpClient client;
    private final AgentRefreshableAccessTokenAuthenticationPersistedData
            redirectTokensPersistedData;

    public StarlingApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        redirectTokensPersistedData =
                new AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory(
                                new ObjectMapperFactory().getInstance())
                        .createAgentRefreshableAccessTokenAuthenticationPersistedData(
                                new AgentAuthenticationPersistedData(persistentStorage));
    }

    public List<AccountEntity> fetchAccounts() {
        return request(StarlingConstants.Url.GET_ACCOUNTS)
                .get(AccountsResponse.class)
                .getAccounts();
    }

    public StarlingAccountHolderType fetchAccountHolderType() {
        return request(Url.GET_ACCOUNT_HOLDER)
                .get(AccountHolderResponse.class)
                .getAccountHolderType();
    }

    public AccountHolderNameResponse fetchAccountHolderName() {
        return request(Url.GET_ACCOUNT_HOLDER_NAME).get(AccountHolderNameResponse.class);
    }

    public SoleTraderAccountHolderResponse fetchSoleTraderAccountHolder() {
        return request(Url.GET_SOLE_TRADER_ACCOUNT_HOLDER)
                .get(SoleTraderAccountHolderResponse.class);
    }

    public BusinessAccountHolderResponse fetchBusinessAccountHolder() {
        return request(Url.GET_BUSINESS_ACCOUNT_HOLDER).get(BusinessAccountHolderResponse.class);
    }

    public AccountIdentifiersResponse fetchAccountIdentifiers(final String accountUid) {
        return request(
                        StarlingConstants.Url.GET_ACCOUNT_IDENTIFIERS.parameter(
                                StarlingConstants.UrlParams.ACCOUNT_UID, accountUid))
                .get(AccountIdentifiersResponse.class);
    }

    public AccountBalanceResponse fetchAccountBalance(final String accountUid) {
        return request(
                        StarlingConstants.Url.GET_ACCOUNT_BALANCE.parameter(
                                StarlingConstants.UrlParams.ACCOUNT_UID, accountUid))
                .get(AccountBalanceResponse.class);
    }

    public TransactionsResponse fetchTransactions(
            Date from, Date to, TransactionalAccount account) {

        URL targetURL =
                Url.GET_ANY_TRANSACTIONS
                        .parameter(UrlParams.ACCOUNT_UID, account.getApiIdentifier())
                        .parameter(
                                UrlParams.CATEGORY_UID,
                                account.getFromTemporaryStorage(UrlParams.CATEGORY_UID));

        return request(targetURL)
                .queryParam(StarlingConstants.RequestKey.FROM, toFormattedDate(from))
                .queryParam(StarlingConstants.RequestKey.TO, toFormattedDate(to))
                .get(TransactionsResponse.class);
    }

    private RequestBuilder request(URL url) {
        return client.request(url)
                .addBearerToken(this.getOAuthToken())
                .accept(MediaType.APPLICATION_JSON);
    }

    private OAuth2Token getOAuthToken() {
        Token accessToken =
                redirectTokensPersistedData.getRefreshableAccessToken().get().getAccessToken();
        return OAuth2Token.create(
                accessToken.getTokenType(),
                new String(accessToken.getBody(), StandardCharsets.UTF_8),
                null,
                accessToken.getExpiresInSeconds());
    }

    private static String toFormattedDate(final Date date) {
        return ThreadSafeDateFormat.FORMATTER_MILLISECONDS_WITHOUT_TIMEZONE.format(date);
    }
}
