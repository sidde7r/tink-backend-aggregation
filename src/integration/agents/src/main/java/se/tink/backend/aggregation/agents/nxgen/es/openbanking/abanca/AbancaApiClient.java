package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.ChallengeType;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.SupplementalFields;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration.AbancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.rpc.SepaPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.rpc.SepaPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.entity.DetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.entity.ErrorsEntity;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.UserAvailability;

@Slf4j
public class AbancaApiClient {
    private final TinkHttpClient client;
    private final AbancaConfiguration configuration;
    private final String redirectUrl;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Map<String, Field> supplementalFields;
    private final UserAvailability userAvailability;

    public AbancaApiClient(
            TinkHttpClient client,
            AgentConfiguration<AbancaConfiguration> agentConfiguration,
            SessionStorage sessionStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            List<Field> supplementalFields,
            UserAvailability userAvailability) {
        this.client = client;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.sessionStorage = sessionStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.supplementalFields =
                supplementalFields.stream()
                        .collect(Collectors.toMap(Field::getName, field -> field));
        this.userAvailability = userAvailability;
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
            return retryRequestWithScaAuthentication(request, response, e);
        }
    }

    private <T> T postWithChallenge(RequestBuilder request, Class<T> response) {
        try {
            return request.post(response);
        } catch (HttpResponseException e) {
            return retryRequestWithScaAuthentication(request, response, e);
        }
    }

    private <T> T retryRequestWithScaAuthentication(
            RequestBuilder request, Class<T> response, HttpResponseException e) {
        if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN && e.getResponse().hasBody()) {
            ErrorsEntity challengeError =
                    e.getResponse()
                            .getBody(ErrorResponse.class)
                            .getChallengeError()
                            .orElseThrow(() -> e);

            if (isUserNotAvailableForInteraction()) {
                log.warn("SCA request in non-manual refresh, this will time out");
            }
            String challengeSolution = requestChallengeSolution(challengeError.getDetails());
            RequestBuilder builder =
                    request.header(
                                    HeaderKeys.CHALLENGE_ID,
                                    challengeError.getDetails().getChallengeId())
                            .header(HeaderKeys.CHALLENGE_RESPONSE, challengeSolution);

            String httpMethod = e.getRequest().getMethod().name();

            if (HttpMethod.POST.equals(httpMethod)) {
                return builder.post(response);
            }
            if (HttpMethod.GET.equals(httpMethod)) {
                return builder.get(response);
            }
        }
        throw e;
    }

    private boolean isUserNotAvailableForInteraction() {
        return !this.userAvailability.isUserAvailableForInteraction();
    }

    private String requestChallengeSolution(DetailsEntity challengeDetails) {
        final String challengeType = challengeDetails.getType();
        final Field descriptionField;
        final Field inputField = supplementalFields.get(SupplementalFields.CHALLENGE_RESPONSE);

        log.info("SCA type {}", challengeType);
        if (challengeType.equalsIgnoreCase(ChallengeType.OTP_SMS)) {
            // hint is masked phone number
            descriptionField = supplementalFields.get(SupplementalFields.OTP_SMS_DESCRIPTION);
            descriptionField.setValue(challengeDetails.getSolutionHint());
        } else if (challengeType.equalsIgnoreCase(ChallengeType.OTP_MOBILE)) {
            // hint can be entered in app to generate code if push notification is not received
            descriptionField = supplementalFields.get(SupplementalFields.OTP_MOBILE_DESCRIPTION);
            descriptionField.setValue(challengeDetails.getSolutionHint());
        } else if (challengeType.equalsIgnoreCase(ChallengeType.OTP_DEVICE)) {
            // no hint, OTP generated with device
            descriptionField = supplementalFields.get(SupplementalFields.OTP_DEVICE_DESCRIPTION);
        } else {
            throw new IllegalStateException(
                    String.format("Unknown challenge type %s", challengeType));
        }

        return supplementalInformationHelper
                .askSupplementalInformation(descriptionField, inputField)
                .get(inputField.getName());
    }

    public AccountsResponse fetchAccounts() {
        AccountsResponse response =
                getWithChallenge(
                        createRequestInSession(AbancaConstants.Urls.ACCOUNTS),
                        AccountsResponse.class);
        addAccountsToSessionStorage(response);
        return response;
    }

    private void addAccountsToSessionStorage(AccountsResponse response) {
        sessionStorage.put("ACCOUNTS", response);
    }

    public BalanceResponse fetchBalance(String accountId) {
        return getWithChallenge(
                createRequestInSession(
                        Urls.BALANCE.parameter(
                                AbancaConstants.UrlParameters.ACCOUNT_ID, accountId)),
                BalanceResponse.class);
    }

    public SepaPaymentResponse createPayment(String accountId, SepaPaymentRequest request) {
        return postWithChallenge(
                createRequestInSession(Urls.PAYMENT.parameter(UrlParameters.ACCOUNT_ID, accountId))
                        .body(request),
                SepaPaymentResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> fetchTransactions(
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
