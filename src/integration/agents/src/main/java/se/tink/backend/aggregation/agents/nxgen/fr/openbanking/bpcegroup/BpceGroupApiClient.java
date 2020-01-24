package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.consent.ConsentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.consent.CustomerConsent;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class BpceGroupApiClient {

    private static final String ENDPOINT_ACCOUNTS = "accounts";
    private static final String ENDPOINT_CONSENTS = "consents";
    private static final String ENDPOINT_TRANSACTIONS = "accounts/{account-id}/transactions";

    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;
    private final BpceGroupConfiguration bpceGroupConfiguration;
    private final BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGenerator;

    public URL getAuthorizeUrl(String state) {
        return httpClient
                .request(createUrl(bpceGroupConfiguration.getAuthorizeUrl()))
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
                        .request(createUrl(bpceGroupConfiguration.getTokenUrl()))
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
                        .request(createUrl(bpceGroupConfiguration.getTokenUrl()))
                        .body(refreshRequest, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class);

        return response.getBody(TokenResponse.class);
    }

    public void recordCustomerConsent(List<String> accountIds) {
        final CustomerConsent customerConsent = createCustomerConsent(accountIds);

        createRequestInSession(createUrl(bpceGroupConfiguration.getBaseUrl(), ENDPOINT_CONSENTS))
                .body(customerConsent)
                .put(HttpResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(
                        createUrl(bpceGroupConfiguration.getBaseUrl(), ENDPOINT_ACCOUNTS))
                .get(AccountsResponse.class);
    }

    public void storeAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, accessToken);
    }

    public TransactionsResponse getTransactions(
            String resourceId, LocalDate dateFrom, LocalDate dateTo) {
        try {
            return getTransactionsBatch(resourceId, dateFrom, dateTo);
        } catch (HttpResponseException e) {
            // 204 means that there is no more transactions for given criteria, so empty response
            // should be returned
            if (e.getResponse().getStatus() == 204) {
                return new TransactionsResponse();
            }
            throw e;
        }
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String requestId = UUID.randomUUID().toString();
        final OAuth2Token token = getTokenFromSession();
        final String signature =
                bpceGroupSignatureHeaderGenerator.buildSignatureHeader(
                        token.toAuthorizeHeader(), requestId);

        return httpClient
                .request(url)
                .addBearerToken(token)
                .header(BpceGroupHttpHeaders.SIGNATURE.getName(), signature)
                .header(BpceGroupHttpHeaders.X_REQUEST_ID.getName(), requestId);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token."));
    }

    private TransactionsResponse getTransactionsBatch(
            String resourceId, LocalDate dateFrom, LocalDate dateTo) {
        return createRequestInSession(
                        createUrlForResource(
                                bpceGroupConfiguration.getBaseUrl(),
                                ENDPOINT_TRANSACTIONS,
                                resourceId))
                .queryParam("dateFrom", DateTimeFormatter.ISO_DATE.format(dateFrom))
                .queryParam("dateTo", DateTimeFormatter.ISO_DATE.format(dateTo))
                .get(TransactionsResponse.class);
    }

    private static URL createUrl(String url) {
        return new URL(url);
    }

    private static URL createUrl(String base, String path) {
        return new URL(String.format("%s/%s", base, path));
    }

    private static URL createUrlForResource(String base, String path, String resourceId) {
        return createUrl(base, path).parameter("accountResourceId", resourceId);
    }

    private static CustomerConsent createCustomerConsent(List<String> accountIds) {
        final List<ConsentDataEntity> consentEntities =
                accountIds.stream().map(ConsentDataEntity::new).collect(Collectors.toList());

        return CustomerConsent.builder()
                .balances(consentEntities)
                .transactions(consentEntities)
                .build();
    }
}
