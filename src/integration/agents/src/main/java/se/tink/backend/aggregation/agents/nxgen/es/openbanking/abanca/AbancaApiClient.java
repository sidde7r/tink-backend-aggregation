package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.commons.httpclient.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.UserMessage;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration.AbancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.entity.ErrorsEntity;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class AbancaApiClient {

    private final TinkHttpClient client;
    private final Catalog catalog;
    private final AbancaConfiguration configuration;
    private final String redirectUrl;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;
    private final SupplementalRequester supplementalRequester;

    public AbancaApiClient(
            TinkHttpClient client,
            Catalog catalog,
            AgentConfiguration<AbancaConfiguration> agentConfiguration,
            SessionStorage sessionStorage,
            Credentials credentials,
            SupplementalRequester supplementalRequester) {
        this.client = client;
        this.catalog = catalog;
        this.configuration = agentConfiguration.getClientConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
        this.supplementalRequester = supplementalRequester;
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format(
                                                AbancaConstants.ErrorMessages.INVALID_CONFIGURATION,
                                                "Redirect URL")));
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Token not found!"));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(HeaderKeys.AUTH_KEY, configuration.getApiKey())
                .addBearerToken(getTokenFromSession());
    }

    private <T> T getWithChallenge(RequestBuilder request, Class<T> response) {
        try {
            return request.get(response);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                if (e.getResponse().hasBody()) {
                    ErrorsEntity challengeError =
                            e.getResponse()
                                    .getBody(ErrorResponse.class)
                                    .getChallengeError()
                                    .orElseThrow(() -> e);

                    String challengeSolution = requestChallengeSolution();
                    if (Strings.isNullOrEmpty(challengeSolution)) {
                        throw new RuntimeException(
                                catalog.getString(UserMessage.INVALID_CHALLENGE_RESPONSE));
                    }

                    return request.header(
                                    HeaderKeys.CHALLENGE_ID,
                                    challengeError.getDetails().getChallengeId())
                            .header(HeaderKeys.CHALLENGE_RESPONSE, challengeSolution)
                            .get(response);
                }
            }
            throw e;
        }
    }

    private String requestChallengeSolution() {
        Field challengeField =
                Field.builder()
                        .description(
                                catalog.getString(UserMessage.GET_CHALLENGE_RESPONSE_DESCRIPTION))
                        .name("response")
                        .build();
        List<Field> fields = Lists.newArrayList(challengeField);

        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));
        String supplemental =
                supplementalRequester.requestSupplementalInformation(credentials, true);
        Map<String, String> answers =
                SerializationUtils.deserializeFromString(
                        supplemental, new TypeReference<Map<String, String>>() {});
        return answers.get("response");
    }

    public AccountsResponse fetchAccounts() {
        return getWithChallenge(
                createRequestInSession(AbancaConstants.Urls.ACCOUNTS), AccountsResponse.class);
    }

    public BalanceResponse fetchBalance(String accountId) {
        return getWithChallenge(
                createRequestInSession(
                        Urls.BALANCE.parameter(
                                AbancaConstants.UrlParameters.ACCOUNT_ID, accountId)),
                BalanceResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> fetchTranscations(
            TransactionalAccount account, String key) {

        URL url =
                Optional.ofNullable(key)
                        .map(s -> new URL(AbancaConstants.Urls.BASE_API_URL + key))
                        .orElseGet(
                                () ->
                                        AbancaConstants.Urls.TRANSACTIONS.parameter(
                                                AbancaConstants.UrlParameters.ACCOUNT_ID,
                                                account.getApiIdentifier()));

        return getWithChallenge(createRequestInSession(url), TransactionsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        return client.request(
                        Urls.AUTHORIZATION.parameter(
                                UrlParameters.CLIENT_ID, configuration.getClientId()))
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.REDIRECT_URI, getRedirectUrl())
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(TokenRequest tokenRequest) {
        return client.request(Urls.TOKEN)
                .header(HeaderKeys.AUTH_KEY, configuration.getApiKey())
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenRequest)
                .toTinkToken();
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
