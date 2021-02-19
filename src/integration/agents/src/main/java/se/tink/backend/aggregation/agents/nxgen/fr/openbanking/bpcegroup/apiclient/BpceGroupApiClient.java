package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.storage.BpceOAuth2TokenStorage;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.consent.ConsentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.consent.CustomerConsent;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class BpceGroupApiClient implements FrAispApiClient {

    private static final String AUTHORIZE_PATH = "/stet/psd2/oauth/authorize";
    private static final String TOKEN_PATH = "/stet/psd2/oauth/token";
    private static final String BASE_PATH = "/stet/psd2/v1";

    private static final String ACCOUNT_RESOURCE_ID_KEY = "account-id";
    private static final String ENDPOINT_ACCOUNTS = "/accounts";
    private static final String ENDPOINT_CONSENTS = "/consents";
    private static final String ENDPOINT_TRUSTED_BENEFICIARIES = "/trusted-beneficiaries";
    private static final String ENDPOINT_BALANCES =
            "/accounts/{" + ACCOUNT_RESOURCE_ID_KEY + "}/balances";
    private static final String ENDPOINT_TRANSACTIONS =
            "/accounts/{" + ACCOUNT_RESOURCE_ID_KEY + "}/transactions";
    private static final String ENDPOINT_TRANSACTIONS_PAGE = ENDPOINT_TRANSACTIONS + "?page=";

    private final TinkHttpClient httpClient;
    private final BpceOAuth2TokenStorage bpceOAuth2TokenStorage;
    private final BpceGroupConfiguration bpceGroupConfiguration;
    private final String redirectUrl;
    private final BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGenerator;

    public URL getAuthorizeUrl(String state) {
        return httpClient
                .request(createUrl(AUTHORIZE_PATH))
                .queryParam("client_id", bpceGroupConfiguration.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", "aisp")
                .queryParam("redirect_uri", redirectUrl)
                .queryParam("state", state)
                .getUrl();
    }

    public TokenResponse exchangeAuthorizationToken(String code) {
        final TokenRequest tokenRequest =
                TokenRequest.builder()
                        .clientId(bpceGroupConfiguration.getClientId())
                        .code(code)
                        .redirectUri(redirectUrl)
                        .build();

        final HttpResponse response =
                httpClient
                        .request(createUrl(TOKEN_PATH))
                        .body(tokenRequest, MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(HttpResponse.class);

        return response.getBody(TokenResponse.class);
    }

    public TokenResponse exchangeRefreshToken(String refreshToken) {
        final RefreshRequest refreshRequest =
                new RefreshRequest(bpceGroupConfiguration.getClientId(), refreshToken);

        final HttpResponse response =
                httpClient
                        .request(createUrl(TOKEN_PATH))
                        .body(refreshRequest, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class);

        return response.getBody(TokenResponse.class);
    }

    public void recordCustomerConsent(List<String> accountIds) {
        final CustomerConsent customerConsent = createCustomerConsent(accountIds);
        final RequestBuilder requestBuilder =
                httpClient
                        .request(createUrlWithBasePath(ENDPOINT_CONSENTS))
                        .body(customerConsent, MediaType.APPLICATION_JSON);

        sendRequestAndGetResponse(requestBuilder, HttpMethod.PUT, HttpResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        final RequestBuilder requestBuilder =
                httpClient.request(createUrlWithBasePath(ENDPOINT_ACCOUNTS));

        return sendRequestAndGetResponse(requestBuilder, HttpMethod.GET, AccountsResponse.class);
    }

    public BalancesResponse fetchBalances(String resourceId) {
        final RequestBuilder requestBuilder =
                httpClient.request(createUrlForResource(ENDPOINT_BALANCES, resourceId));

        return sendRequestAndGetResponse(requestBuilder, HttpMethod.GET, BalancesResponse.class);
    }

    public TransactionsResponse getTransactions(String resourceId) {
        return getTransactionsBatch(resourceId, ENDPOINT_TRANSACTIONS);
    }

    public TransactionsResponse getTransactions(String resourceId, int page) {
        return getTransactionsBatch(resourceId, ENDPOINT_TRANSACTIONS_PAGE + page);
    }

    private TransactionsResponse getTransactionsBatch(String resourceId, String url) {
        final RequestBuilder requestBuilder =
                httpClient.request(createUrlForResource(url, resourceId));

        return sendRequestAndGetResponse(
                requestBuilder, HttpMethod.GET, TransactionsResponse.class);
    }

    private <T> T sendRequestAndGetResponse(
            RequestBuilder requestBuilder, HttpMethod httpMethod, Class<T> clazz) {
        return addHeadersToRequest(requestBuilder, httpMethod).method(httpMethod, clazz);
    }

    private RequestBuilder addHeadersToRequest(
            RequestBuilder requestBuilder, HttpMethod httpMethod) {
        final String requestId = UUID.randomUUID().toString();
        final OAuth2Token token = bpceOAuth2TokenStorage.getToken();
        final String signature =
                bpceGroupSignatureHeaderGenerator.buildSignatureHeader(
                        httpMethod, requestBuilder.getUrl(), requestId);

        return requestBuilder
                .addBearerToken(token)
                .header(Psd2Headers.Keys.PSU_IP_ADDRESS, "127.0.0.1")
                .header(Psd2Headers.Keys.SIGNATURE, signature)
                .header(Psd2Headers.Keys.X_REQUEST_ID, requestId);
    }

    private URL createUrl(String path) {
        return new URL(String.format("%s%s", bpceGroupConfiguration.getServerUrl(), path));
    }

    private URL createUrlWithBasePath(String path) {
        return new URL(
                String.format("%s%s%s", bpceGroupConfiguration.getServerUrl(), BASE_PATH, path));
    }

    private URL createUrlForResource(String path, String resourceId) {
        return createUrlWithBasePath(path).parameter(ACCOUNT_RESOURCE_ID_KEY, resourceId);
    }

    private static CustomerConsent createCustomerConsent(List<String> accountIds) {
        final List<ConsentDataEntity> consentEntities =
                accountIds.stream().map(ConsentDataEntity::new).collect(Collectors.toList());

        return CustomerConsent.builder()
                .balances(consentEntities)
                .transactions(consentEntities)
                .psuIdentity(true)
                .trustedBeneficiaries(true)
                .build();
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries() {
        return getTrustedBeneficiaries(ENDPOINT_TRUSTED_BENEFICIARIES);
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries(String path) {
        final RequestBuilder requestBuilder =
                addHeadersToRequest(
                        httpClient.request(createUrlWithBasePath(path)), HttpMethod.GET);

        final HttpResponse httpResponse =
                sendRequestAndGetResponse(requestBuilder, HttpMethod.GET, HttpResponse.class);

        if (HttpStatus.SC_NO_CONTENT == httpResponse.getStatus()) {
            return Optional.empty();
        }

        return Optional.of(httpResponse.getBody(TrustedBeneficiariesResponseDto.class));
    }
}
