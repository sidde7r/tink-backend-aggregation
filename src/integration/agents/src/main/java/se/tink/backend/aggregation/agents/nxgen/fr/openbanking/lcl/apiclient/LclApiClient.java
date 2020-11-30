package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient;

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclAgent;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.RetrieveTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.identity.EndUserIdentityResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

@RequiredArgsConstructor
@Slf4j
public class LclApiClient implements FrAispApiClient {

    private static final String TOKEN_PATH = "/token";
    private static final String AISP_PATH = "/aisp";
    private static final String ACCOUNTS_PATH = AISP_PATH + "/accounts";
    private static final String TRANSACTIONS_PATH = ACCOUNTS_PATH + "/%s/transactions?page=%d";
    private static final String END_USER_IDENTITY_PATH = AISP_PATH + "/end-user-identity";
    private static final String TRUSTED_BENEFICIARIES_PATH = AISP_PATH + "/trusted-beneficiaries";

    private final TinkHttpClient httpClient;
    private final LclHeaderValueProvider headerValueProvider;
    private final OAuth2TokenStorage tokenStorage;
    private final AgentConfiguration<LclConfiguration> agentConfiguration;

    @SneakyThrows
    public TokenResponseDto retrieveAccessToken(String code) {
        final RetrieveTokenRequest request =
                new RetrieveTokenRequest(
                        LclAgent.CLIENT_ID, agentConfiguration.getRedirectUrl(), code);

        return sendTokenRequestAndGetResponse(request);
    }

    @SneakyThrows
    public Optional<TokenResponseDto> refreshAccessToken(String refreshToken) {
        try {
            final RefreshTokenRequest request =
                    new RefreshTokenRequest(LclAgent.CLIENT_ID, refreshToken);
            final TokenResponseDto response = sendTokenRequestAndGetResponse(request);

            return Optional.ofNullable(response);
        } catch (BankServiceException ex) {
            log.error("Refresh token failed.");
            log.error(ex.getMessage(), ex);
        }

        return Optional.empty();
    }

    public AccountsResponseDto getAccountsResponse() {
        return sendGetRequestAndGetResponse(ACCOUNTS_PATH, AccountsResponseDto.class);
    }

    public TransactionsResponseDto getTransactionsResponse(String resourceId, int page) {
        final String path = String.format(TRANSACTIONS_PATH, resourceId, page);

        return sendGetRequestAndGetResponse(path, TransactionsResponseDto.class);
    }

    public EndUserIdentityResponseDto getEndUserIdentity() {
        return sendGetRequestAndGetResponse(
                END_USER_IDENTITY_PATH, EndUserIdentityResponseDto.class);
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries() {
        return getTrustedBeneficiaries(createUrl(TRUSTED_BENEFICIARIES_PATH));
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries(String url) {
        return Optional.of(
                sendGetRequestForUrlAndGetResponse(url, TrustedBeneficiariesResponseDto.class));
    }

    private <T> T sendGetRequestAndGetResponse(String path, Class<T> clazz) {
        return sendGetRequestForUrlAndGetResponse(createUrl(path), clazz);
    }

    private <T> T sendGetRequestForUrlAndGetResponse(String url, Class<T> clazz) {
        return createRequestAndSetHeaders(url, null).addBearerToken(getOAuth2Token()).get(clazz);
    }

    private TokenResponseDto sendTokenRequestAndGetResponse(Object body) {
        return createRequestAndSetHeaders(createUrl(TOKEN_PATH), body)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponseDto.class, body);
    }

    private String createUrl(String path) {
        return LclAgent.BASE_URL + path;
    }

    private RequestBuilder createRequestAndSetHeaders(String url, Object body) {
        final String requestId = UUID.randomUUID().toString();
        final String date = headerValueProvider.getDateHeaderValue();
        final String digest = headerValueProvider.getDigestHeaderValue(body);
        final String signature =
                headerValueProvider.getSignatureHeaderValue(requestId, date, digest);

        return httpClient
                .request(url)
                .header(Psd2Headers.Keys.X_REQUEST_ID, requestId)
                .header(Psd2Headers.Keys.DATE, date)
                .header(Psd2Headers.Keys.DIGEST, digest)
                .header(Psd2Headers.Keys.SIGNATURE, signature);
    }

    private OAuth2Token getOAuth2Token() {
        return tokenStorage
                .getToken()
                .orElseThrow(
                        () -> new IllegalArgumentException("Access token not found in storage."));
    }
}
