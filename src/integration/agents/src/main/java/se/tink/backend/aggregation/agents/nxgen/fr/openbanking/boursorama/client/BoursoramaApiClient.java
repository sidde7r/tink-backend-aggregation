package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration.BoursoramaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.IdentityEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BoursoramaApiClient {

    private static final SimpleDateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final TinkHttpClient client;
    private final BoursoramaConfiguration configuration;

    public BoursoramaApiClient(TinkHttpClient client, BoursoramaConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    public TokenResponse exchangeAuthorizationCode(TokenRequest tokenRequest)
            throws JsonProcessingException {

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
            String userHash, String resourceId, Date dateFrom, Date dateTo) {

        return baseAISRequest(Urls.TRANSACTIONS_TEMPLATE + resourceId, userHash)
                // FIXME transaction pagination does not work on sandbox - adding params results in
                // status 403
                .queryParam("dateFrom", API_DATE_FORMAT.format(dateFrom))
                .queryParam("dateTo", API_DATE_FORMAT.format(dateTo))
                .get(TransactionsResponse.class);
    }

    private RequestBuilder baseAISRequest(String urlTemplate, String userHash) {
        String url = String.format(urlTemplate, userHash);
        return client.request(configuration.getBaseUrl() + url).type(MediaType.APPLICATION_JSON);
    }
}
