package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import java.time.LocalDate;
import java.util.Optional;
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
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDtoBase;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class BoursoramaApiClient implements FrAispApiClient {

    private final TinkHttpClient client;
    private final BoursoramaConfiguration configuration;
    private final SessionStorage sessionStorage;

    public TokenResponse exchangeAuthorizationCode(TokenRequest tokenRequest) {
        return client.request(Urls.CONSUME_AUTH_CODE)
                .body(tokenRequest, MediaType.APPLICATION_JSON)
                .post(TokenResponse.class);
    }

    public TokenResponse refreshToken(RefreshTokenRequest tokenRequest) {
        return client.request(Urls.REFRESH_TOKEN)
                .body(tokenRequest, MediaType.APPLICATION_JSON)
                .post(TokenResponse.class);
    }

    public IdentityEntity fetchIdentityData() {
        return baseAISRequest(Urls.IDENTITY_TEMPLATE).get(IdentityEntity.class);
    }

    public AccountsResponse fetchAccounts() {
        return baseAISRequest(Urls.ACCOUNTS_TEMPLATE).get(AccountsResponse.class);
    }

    public BalanceResponse fetchBalances(String resourceId) {
        return baseAISRequest(Urls.BALANCES_TEMPLATE.parameter("resourceId", resourceId))
                .get(BalanceResponse.class);
    }

    public TransactionsResponse fetchTransactions(
            String resourceId, LocalDate dateFrom, LocalDate dateTo) {
        return baseAISRequest(Urls.TRANSACTIONS_TEMPLATE.parameter("resourceId", resourceId))
                .queryParam("dateFrom", dateFrom.toString())
                .queryParam("dateTo", dateTo.toString())
                .get(TransactionsResponse.class);
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries() {
        return Optional.of(
                baseAISRequest(Urls.TRUSTED_BENEFICIARIES_TEMPLATE)
                        .get(TrustedBeneficiariesResponseDto.class));
    }

    @Override
    public Optional<? extends TrustedBeneficiariesResponseDtoBase> getTrustedBeneficiaries(
            String path) {
        return Optional.empty();
    }

    private RequestBuilder baseAISRequest(URL urlTemplate) {
        final String userHash = sessionStorage.get(BoursoramaConstants.USER_HASH);
        final URL url = urlTemplate.parameter("userHash", userHash);

        return client.request(url)
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
