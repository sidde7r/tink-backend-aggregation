package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration.BoursoramaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.IdentityEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class BoursoramaApiClient {

    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final TinkHttpClient client;
    private final BoursoramaConfiguration configuration;

    public BoursoramaApiClient(TinkHttpClient client, BoursoramaConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
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
            String userHash, String resourceId, Date dateFrom, Date dateTo) {

        return baseAISRequest(Urls.TRANSACTIONS_TEMPLATE + resourceId, userHash)
                .queryParam("dateFrom", apiDateFormat.format(dateFrom))
                .queryParam("dateTo", apiDateFormat.format(dateTo))
                .get(TransactionsResponse.class);
    }

    private RequestBuilder baseAISRequest(String urlTemplate, String userHash) {
        String url = String.format(urlTemplate, userHash);
        return client.request(configuration.getBaseUrl() + url).type(MediaType.APPLICATION_JSON);
    }
}
