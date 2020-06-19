package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration.BoursoramaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.IdentityEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class BoursoramaApiClient {

    private final TinkHttpClient client;
    private final BoursoramaConfiguration configuration;
    private final SessionStorage sessionStorage;

    public TokenResponse exchangeAuthorizationCode(TokenRequest tokenRequest) {
        return client.request(configuration.getBaseUrl() + Urls.CONSUME_AUTH_CODE)
                .body(tokenRequest, MediaType.APPLICATION_JSON)
                .post(TokenResponse.class);
    }

    public TokenResponse refreshToken(RefreshTokenRequest tokenRequest) {
        return client.request(configuration.getBaseUrl() + Urls.REFRESH_TOKEN)
                .body(tokenRequest, MediaType.APPLICATION_JSON)
                .post(TokenResponse.class);
    }

    public IdentityEntity fetchIdentityData(String userHash) {
        return baseAISRequest(Urls.IDENTITY_TEMPLATE, userHash).get(IdentityEntity.class);
    }

    public AccountsResponse fetchAccounts(String userHash) {
        return baseAISRequest(Urls.ACCOUNTS_TEMPLATE, userHash).get(AccountsResponse.class);
    }

    public BalanceResponse fetchBalances(String userHash, String resourceId) {
        return baseAISRequest(Urls.BALANCES_TEMPLATE + resourceId, userHash)
                .get(BalanceResponse.class);
    }

    public TransactionsResponse fetchTransactions(
            String userHash, String resourceId, LocalDate dateFrom, LocalDate dateTo) {

        return baseAISRequest(Urls.TRANSACTIONS_TEMPLATE + resourceId, userHash)
                .queryParam("dateFrom", dateFrom.toString())
                .queryParam("dateTo", dateTo.toString())
                .get(TransactionsResponse.class);
    }

    private RequestBuilder baseAISRequest(String urlTemplate, String userHash) {
        String url = String.format(urlTemplate, userHash);
        return client.request(configuration.getBaseUrl() + url)
                .type(MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromStorage());
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(BoursoramaConstants.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        SessionError.SESSION_EXPIRED.exception()));
    }
}
