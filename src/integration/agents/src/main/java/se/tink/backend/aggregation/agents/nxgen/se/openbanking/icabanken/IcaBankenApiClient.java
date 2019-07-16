package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

import com.datastax.driver.core.utils.UUIDs;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.Account;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class IcaBankenApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private IcaBankenConfiguration configuration;

    public IcaBankenApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public TokenResponse exchangeAuthorizationCode(AuthorizationRequest request) {
        return client.request(new URL(Urls.TOKEN_PATH))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.TINK_DEBUG, HeaderKeys.TRUST_ALL)
                .post(TokenResponse.class);
    }

    public TokenResponse exchangeRefreshToken(RefreshTokenRequest request) {
        return client.request(new URL(Urls.TOKEN_PATH))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.TINK_DEBUG, HeaderKeys.TRUST_ALL)
                .post(TokenResponse.class);
    }

    public FetchAccountsResponse fetchAccounts() {

        return client.request(new URL(Urls.ACCOUNTS_PATH))
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.WITH_BALANCE)
                .header(
                        HeaderKeys.AUTHORIZATION,
                        HeaderValues.BEARER + sessionStorage.get(StorageKeys.TOKEN))
                .header(HeaderKeys.SCOPE, HeaderValues.ACCOUNT)
                .header(HeaderKeys.REQUEST_ID, UUIDs.random().toString())
                .header(HeaderKeys.TINK_DEBUG, HeaderKeys.TRUST_ALL)
                .get(FetchAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactionsForAccount(
            String apiIdentifier, Date fromDate, Date toDate) {
        final URL baseUrl = new URL(Urls.TRANSACTIONS_PATH);
        final URL requestUrl = baseUrl.parameter(Account.ACCOUNT_ID, apiIdentifier);

        return client.request(requestUrl)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(QueryKeys.STATUS, QueryValues.STATUS)
                .header(HeaderKeys.REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        HeaderKeys.AUTHORIZATION,
                        HeaderValues.BEARER + sessionStorage.get(StorageKeys.TOKEN))
                .header(HeaderKeys.SCOPE, HeaderValues.ACCOUNT)
                .header(HeaderKeys.TINK_DEBUG, HeaderKeys.TRUST_ALL)
                .get(FetchTransactionsResponse.class);
    }

    public void setConfiguration(IcaBankenConfiguration icaBankenConfiguration) {
        this.configuration = icaBankenConfiguration;
    }
}
