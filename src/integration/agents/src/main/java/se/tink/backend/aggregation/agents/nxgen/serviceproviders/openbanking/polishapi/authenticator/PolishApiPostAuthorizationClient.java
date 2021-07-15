package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.GrantTypes.AUTHORIZATION_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.GrantTypes.EXCHANGE_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.GrantTypes.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.SCOPE_USAGE_LIMIT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.Scopes.AIS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.Scopes.AIS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.PostClient.AUTHORIZATION_MODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.PostClient.THROTTLING_POLICY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.X_REQUEST_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Localization.DATE_TIME_FORMATTER_REQUEST_HEADERS;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.common.ScopeDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.authorize.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeItemWithHistoryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.token.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.BasePolishApiPostClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrorHandler;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class PolishApiPostAuthorizationClient extends BasePolishApiPostClient
        implements PolishApiAuthorizationClient {

    private final PolishAuthorizeApiUrlFactory urlFactory;
    private final Integer maxDaysToFetch;

    public PolishApiPostAuthorizationClient(
            PolishAuthorizeApiUrlFactory urlFactory,
            TinkHttpClient httpClient,
            AgentConfiguration<PolishApiConfiguration> configuration,
            AgentComponentProvider agentComponentProvider,
            PolishApiPersistentStorage persistentStorage,
            Integer maxDaysToFetch) {
        super(httpClient, agentComponentProvider, configuration, persistentStorage);
        this.urlFactory = urlFactory;
        this.maxDaysToFetch = maxDaysToFetch;
    }

    @Override
    public URL getAuthorizeUrl(String state) {
        String requestId = getUuid();
        String consentId = getUuid();
        ZonedDateTime requestTime = getNow();
        persistentStorage.persistConsentId(consentId);

        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();
        AuthorizeRequest authorizeRequest =
                AuthorizeRequest.builder()
                        .requestHeader(getRequestHeaderEntity(requestId, requestTime, null))
                        .clientId(apiConfiguration.getApiKey())
                        .clientSecret(apiConfiguration.getClientSecret())
                        .redirectUri(configuration.getRedirectUrl())
                        .responseType(CODE)
                        .scope(AIS_ACCOUNTS)
                        .scopeDetails(
                                ScopeDetailsEntity.builder()
                                        .privilegeList(prepareAisAccountsPrivilegeList())
                                        .consentId(consentId)
                                        .scopeTimeLimit(
                                                getNow().plusDays(90)
                                                        .format(
                                                                DATE_TIME_FORMATTER_REQUEST_HEADERS))
                                        .throttlingPolicy(THROTTLING_POLICY)
                                        .scopeGroupType(AIS_ACCOUNTS)
                                        .build())
                        .authorizationMode(AUTHORIZATION_MODE)
                        .state(state)
                        .build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(urlFactory.getAuthorizeUrl(), requestTime, null)
                        .header(X_REQUEST_ID, requestId)
                        .body(authorizeRequest, MediaType.APPLICATION_JSON);

        AuthorizationResponse authorizationResponse =
                PolishApiErrorHandler.callWithErrorHandling(
                        requestBuilder,
                        AuthorizationResponse.class,
                        PolishApiErrorHandler.RequestType.POST);

        return new URL(authorizationResponse.getRedirectUri());
    }

    private List<PrivilegeListEntity> prepareAisAccountsPrivilegeList() {
        PrivilegeListEntity aisPrivilegeListEntity =
                PrivilegeListEntity.builder()
                        .aisAccountsGetAccounts(
                                PrivilegeItemEntity.builder()
                                        .scopeUsageLimit(SCOPE_USAGE_LIMIT)
                                        .build())
                        .build();

        return Arrays.asList(aisPrivilegeListEntity);
    }

    private List<PrivilegeListEntity> prepareAisPrivilegeList() {
        List<PrivilegeListEntity> privilegeListEntities = new ArrayList<>();
        for (String accountNumber : persistentStorage.getAccountIdentifiers()) {
            privilegeListEntities.add(
                    PrivilegeListEntity.builder()
                            .accountNumber(Arrays.asList(accountNumber))
                            .aisGetAccount(
                                    PrivilegeItemEntity.builder()
                                            .scopeUsageLimit(SCOPE_USAGE_LIMIT)
                                            .build())
                            .aisGetTransactionsDone(
                                    PrivilegeItemWithHistoryEntity.builder()
                                            .scopeUsageLimit(SCOPE_USAGE_LIMIT)
                                            .maxAllowedHistoryLong(maxDaysToFetch)
                                            .build())
                            .aisGetTransactionsPending(
                                    PrivilegeItemWithHistoryEntity.builder()
                                            .scopeUsageLimit(SCOPE_USAGE_LIMIT)
                                            .maxAllowedHistoryLong(maxDaysToFetch)
                                            .build())
                            .build());
        }
        return privilegeListEntities;
    }

    @Override
    public TokenResponse exchangeAuthorizationToken(String accessCode) {
        String requestId = getUuid();
        ZonedDateTime requestTime = getNow();
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();
        TokenRequest tokenRequest =
                TokenRequest.builder()
                        .requestHeader(getRequestHeaderEntity(requestId, requestTime, null))
                        .clientId(apiConfiguration.getApiKey())
                        .scope(AIS_ACCOUNTS)
                        .scopeDetails(
                                ScopeDetailsEntity.builder()
                                        .consentId(persistentStorage.getConsentId())
                                        .scopeTimeLimit(
                                                getNow().plusDays(90)
                                                        .format(
                                                                DATE_TIME_FORMATTER_REQUEST_HEADERS))
                                        .throttlingPolicy(THROTTLING_POLICY)
                                        .scopeGroupType(AIS_ACCOUNTS)
                                        .build())
                        .grantType(AUTHORIZATION_CODE)
                        .code(accessCode)
                        .redirectUri(configuration.getRedirectUrl())
                        .build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(urlFactory.getOauth2TokenUrl(), requestTime, null)
                        .header(X_REQUEST_ID, requestId)
                        .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TokenResponse.class, PolishApiErrorHandler.RequestType.POST);
    }

    @Override
    public TokenResponse exchangeRefreshToken(String refreshToken) {
        String requestId = getUuid();
        ZonedDateTime requestTime = getNow();
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();
        TokenRequest tokenRequest =
                TokenRequest.builder()
                        .requestHeader(
                                getRequestHeaderEntity(
                                        requestId, requestTime, getAccessTokenFromStorage()))
                        .clientId(apiConfiguration.getApiKey())
                        .scope(AIS)
                        .grantType(REFRESH_TOKEN)
                        .refreshToken(refreshToken)
                        .build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                                urlFactory.getOauth2TokenUrl(), requestTime, getTokenFromStorage())
                        .header(X_REQUEST_ID, requestId)
                        .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TokenResponse.class, PolishApiErrorHandler.RequestType.POST);
    }

    @Override
    public TokenResponse exchangeTokenForAis(String refreshToken) {
        String requestId = getUuid();
        ZonedDateTime requestTime = getNow();
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();
        TokenRequest tokenRequest =
                TokenRequest.builder()
                        .requestHeader(
                                getRequestHeaderEntity(
                                        requestId, requestTime, getAccessTokenFromStorage()))
                        .scopeDetails(
                                ScopeDetailsEntity.builder()
                                        .privilegeList(prepareAisPrivilegeList())
                                        .consentId(persistentStorage.getConsentId())
                                        .scopeTimeLimit(
                                                getNow().plusDays(90)
                                                        .format(
                                                                DATE_TIME_FORMATTER_REQUEST_HEADERS))
                                        .throttlingPolicy(THROTTLING_POLICY)
                                        .scopeGroupType(AIS)
                                        .build())
                        .clientId(apiConfiguration.getApiKey())
                        .scope(AIS)
                        .grantType(EXCHANGE_TOKEN)
                        .exchangeToken(refreshToken)
                        .build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                                urlFactory.getOauth2TokenUrl(), requestTime, getTokenFromStorage())
                        .header(X_REQUEST_ID, requestId)
                        .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TokenResponse.class, PolishApiErrorHandler.RequestType.POST);
    }
}
