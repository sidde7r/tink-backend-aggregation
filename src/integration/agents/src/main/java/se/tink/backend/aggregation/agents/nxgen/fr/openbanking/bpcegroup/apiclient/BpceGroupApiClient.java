package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupHttpHeaders;
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
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class BpceGroupApiClient {

    private static final String AUTHORIZE_PATH = "/stet/psd2/oauth/authorize";
    private static final String TOKEN_PATH = "/stet/psd2/oauth/token";
    private static final String BASE_PATH = "/stet/psd2/v1";

    private static final String ACCOUNT_RESOURCE_ID_KEY = "account-id";
    private static final String ENDPOINT_ACCOUNTS = "/accounts";
    private static final String ENDPOINT_CONSENTS = "/consents";
    private static final String ENDPOINT_BALANCES =
            "/accounts/{" + ACCOUNT_RESOURCE_ID_KEY + "}/balances";
    private static final String ENDPOINT_TRANSACTIONS =
            "/accounts/{" + ACCOUNT_RESOURCE_ID_KEY + "}/transactions";

    private final TinkHttpClient httpClient;
    private final BpceOAuth2TokenStorage bpceOAuth2TokenStorage;
    private final BpceGroupConfiguration bpceGroupConfiguration;
    private final BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGenerator;

    public URL getAuthorizeUrl(String state) {
        return httpClient
                .request(createUrl(AUTHORIZE_PATH))
                .queryParam("client_id", bpceGroupConfiguration.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", bpceGroupConfiguration.getAuthScope())
                .queryParam("redirect_uri", bpceGroupConfiguration.getRedirectUrl())
                .queryParam("state", state)
                .getUrl();
    }

    public TokenResponse exchangeAuthorizationToken(String code) {
        final TokenRequest tokenRequest =
                TokenRequest.builder()
                        .clientId(bpceGroupConfiguration.getClientId())
                        .code(code)
                        .redirectUri(bpceGroupConfiguration.getRedirectUrl())
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
                        .request(createUrl(BASE_PATH, ENDPOINT_CONSENTS))
                        .body(customerConsent, MediaType.APPLICATION_JSON);

        sendRequestAndGetResponse(requestBuilder, HttpMethod.PUT, HttpResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        final RequestBuilder requestBuilder =
                httpClient.request(createUrl(BASE_PATH, ENDPOINT_ACCOUNTS));

        return sendRequestAndGetResponse(requestBuilder, HttpMethod.GET, AccountsResponse.class);
    }

    public BalancesResponse fetchBalances(String resourceId) {
        final RequestBuilder requestBuilder =
                httpClient.request(createUrlForResource(BASE_PATH, ENDPOINT_BALANCES, resourceId));

        return sendRequestAndGetResponse(requestBuilder, HttpMethod.GET, BalancesResponse.class);
    }

    public TransactionsResponse getTransactions(
            String resourceId, LocalDate dateFrom, LocalDate dateTo) {
        try {
            return getTransactionsBatch(resourceId, dateFrom, dateTo);
        } catch (HttpResponseException e) {
            /* 204 means that there is no more transactions for given criteria, so empty response should be returned */
            if (e.getResponse().getStatus() == 204) {
                return new TransactionsResponse();
            }
            throw e;
        }
    }

    private TransactionsResponse getTransactionsBatch(
            String resourceId, LocalDate dateFrom, LocalDate dateTo) {

        final RequestBuilder requestBuilder =
                httpClient
                        .request(createUrlForResource(BASE_PATH, ENDPOINT_TRANSACTIONS, resourceId))
                        .queryParam("dateFrom", DateTimeFormatter.ISO_DATE.format(dateFrom))
                        .queryParam("dateTo", DateTimeFormatter.ISO_DATE.format(dateTo));

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
                .header(BpceGroupHttpHeaders.SIGNATURE.getName(), signature)
                .header(BpceGroupHttpHeaders.X_REQUEST_ID.getName(), requestId);
    }

    private URL createUrl(String path) {
        return new URL(String.format("%s%s", bpceGroupConfiguration.getServerUrl(), path));
    }

    private URL createUrl(String base, String path) {
        return new URL(String.format("%s%s%s", bpceGroupConfiguration.getServerUrl(), base, path));
    }

    private URL createUrlForResource(String base, String path, String resourceId) {
        return createUrl(base, path).parameter(ACCOUNT_RESOURCE_ID_KEY, resourceId);
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
}
