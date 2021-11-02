package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.CONSENT_LENGTH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.GrantTypes.AUTHORIZATION_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.GrantTypes.EXCHANGE_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.GrantTypes.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.SCOPE_USAGE_LIMIT_MULTIPLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.SCOPE_USAGE_LIMIT_SINGLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.Scopes.AIS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.Scopes.AIS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.PostClient.THROTTLING_POLICY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderValues.GetClient.PSU_USER_AGENT_VAL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Localization.DATE_TIME_FORMATTER_REQUEST_HEADERS;

import com.google.common.collect.ImmutableList;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.common.ScopeDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.authorize.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.token.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.BasePolishApiPostClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents.PolishApiAgentCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents.PolishApiLogicFlowConfigurator;
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
    private final PolishApiLogicFlowConfigurator apiLogicFlowConfigurator;
    private String scopeTimeLimit;

    public PolishApiPostAuthorizationClient(
            PolishApiAgentCreator polishApiAgentCreator,
            TinkHttpClient httpClient,
            AgentConfiguration<PolishApiConfiguration> configuration,
            AgentComponentProvider agentComponentProvider,
            PolishApiPersistentStorage persistentStorage) {
        super(
                httpClient,
                agentComponentProvider,
                configuration,
                persistentStorage,
                polishApiAgentCreator);
        this.urlFactory = polishApiAgentCreator.getAuthorizeApiUrlFactory();
        this.apiLogicFlowConfigurator = polishApiAgentCreator.getLogicFlowConfigurator();
    }

    @Override
    public URL getAuthorizeUrl(String state) {
        String consentId = getUuid();
        ZonedDateTime requestTime = getNow();
        persistentStorage.persistConsentId(consentId);

        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();
        AuthorizeRequest.AuthorizeRequestBuilder<?, ?> authorizeRequestBuilder =
                AuthorizeRequest.builder()
                        .requestHeader(getRequestHeaderEntity(requestTime, null))
                        .clientId(apiConfiguration.getApiKey())
                        .redirectUri(configuration.getRedirectUrl())
                        .responseType(CODE)
                        .scope(getScopeForAuthorizationToken())
                        .scopeDetails(
                                ScopeDetailsEntity.builder()
                                        .privilegeList(prepareAuthorizeRequestPrivilegeList())
                                        .consentId(consentId)
                                        .scopeTimeLimit(getScopeTimeLimit(CONSENT_LENGTH))
                                        .throttlingPolicy(THROTTLING_POLICY)
                                        .scopeGroupType(getScopeForAuthorizationToken())
                                        .build())
                        .state(state);

        AuthorizeRequest authorizeRequest = authorizeRequestBuilder.build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(urlFactory.getAuthorizeUrl(), requestTime, null)
                        .body(authorizeRequest, MediaType.APPLICATION_JSON);

        AuthorizationResponse authorizationResponse =
                PolishApiErrorHandler.callWithErrorHandling(
                        requestBuilder,
                        AuthorizationResponse.class,
                        PolishApiErrorHandler.RequestType.POST);

        return new URL(authorizationResponse.getRedirectUri());
    }

    protected String getScopeTimeLimit(int consentLength) {
        if (scopeTimeLimit == null) {
            scopeTimeLimit =
                    getNow().plusDays(consentLength).format(DATE_TIME_FORMATTER_REQUEST_HEADERS);
        }
        return scopeTimeLimit;
    }

    private List<PrivilegeListEntity> prepareAuthorizeRequestPrivilegeList() {
        if (shouldUseAisScopeForAuthorizationToken()) {
            if (apiLogicFlowConfigurator.canCombineAisAndAisAccountsScopes()) {
                return ImmutableList.of(
                        PolishApiPostPrivilegeListEntityBuilder.getAisAndAisAccountsPrivileges(
                                getScopeUsageLimit(), getMaxDaysToFetch()));
            } else {
                return ImmutableList.of(
                        PolishApiPostPrivilegeListEntityBuilder.getAisPrivileges(
                                getMaxDaysToFetch()));
            }
        } else {
            return ImmutableList.of(
                    PolishApiPostPrivilegeListEntityBuilder.getAisAccountsPrivileges(
                            getScopeUsageLimit()));
        }
    }

    private String getScopeUsageLimit() {
        return apiLogicFlowConfigurator.shouldSentSingleScopeLimitInAisAccounts()
                ? SCOPE_USAGE_LIMIT_SINGLE
                : SCOPE_USAGE_LIMIT_MULTIPLE;
    }

    private Integer getMaxDaysToFetch() {
        return polishApiAgentCreator.getMaxDaysToFetch();
    }

    @Override
    public TokenResponse exchangeAuthorizationToken(String accessCode) {
        ZonedDateTime requestTime = getNow();
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();
        TokenRequest.TokenRequestBuilder<?, ?> tokenRequestBuilder =
                TokenRequest.builder()
                        .requestHeader(getRequestHeaderEntity(requestTime, null))
                        .clientId(apiConfiguration.getApiKey())
                        .grantType(AUTHORIZATION_CODE)
                        .redirectUri(configuration.getRedirectUrl())
                        .userAgent(PSU_USER_AGENT_VAL)
                        .userIp(getOriginatingUserIp())
                        .isUserSession(isUserPresent());

        setAuthorizationCode(accessCode, tokenRequestBuilder);
        setScopeAndScopeDetailsForAuthorizationToken(tokenRequestBuilder);

        TokenRequest tokenRequest = tokenRequestBuilder.build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(urlFactory.getOauth2TokenUrl(), requestTime, null)
                        .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TokenResponse.class, PolishApiErrorHandler.RequestType.POST);
    }

    private void setAuthorizationCode(
            String accessCode, TokenRequest.TokenRequestBuilder<?, ?> tokenRequestBuilder) {
        if (apiLogicFlowConfigurator.shouldSentAuthorizationCodeInUpperCaseField()) {
            tokenRequestBuilder.codeUpperCase(accessCode);
        } else {
            tokenRequestBuilder.codeLowerCase(accessCode);
        }
    }

    private void setScopeAndScopeDetailsForAuthorizationToken(
            TokenRequest.TokenRequestBuilder<?, ?> tokenRequestBuilder) {
        if (apiLogicFlowConfigurator.shouldSentScopeAndScopeDetailsInFirstTokenRequest()) {
            tokenRequestBuilder
                    .scope(getScopeForAuthorizationToken())
                    .scopeDetails(
                            ScopeDetailsEntity.builder()
                                    .privilegeList(prepareAuthorizeRequestPrivilegeList())
                                    .consentId(persistentStorage.getConsentId())
                                    .scopeTimeLimit(getScopeTimeLimit(CONSENT_LENGTH))
                                    .throttlingPolicy(THROTTLING_POLICY)
                                    .scopeGroupType(getScopeForAuthorizationToken())
                                    .build());
        }
    }

    private String getScopeForAuthorizationToken() {
        if (shouldUseAisScopeForAuthorizationToken()) {
            return AIS;
        } else {
            return AIS_ACCOUNTS;
        }
    }

    private boolean shouldUseAisScopeForAuthorizationToken() {
        return apiLogicFlowConfigurator.canCombineAisAndAisAccountsScopes()
                || (!apiLogicFlowConfigurator.doesSupportExchangeToken()
                        && apiLogicFlowConfigurator.shouldGetAccountListFromTokenResponse());
    }

    @Override
    public TokenResponse exchangeRefreshToken(String refreshToken) {
        ZonedDateTime requestTime = getNow();
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();
        TokenRequest.TokenRequestBuilder<?, ?> tokenRequestBuilder =
                TokenRequest.builder()
                        .requestHeader(
                                getRequestHeaderEntity(
                                        requestTime,
                                        getAccessTokenFromStorage(),
                                        apiLogicFlowConfigurator
                                                .shouldSentTokenInRefreshAndExchangeToken(),
                                        true))
                        .clientId(apiConfiguration.getApiKey())
                        .grantType(REFRESH_TOKEN)
                        .refreshToken(refreshToken)
                        .userAgent(PSU_USER_AGENT_VAL)
                        .userIp(getOriginatingUserIp())
                        .isUserSession(isUserPresent());

        if (apiLogicFlowConfigurator.shouldSentScopeInRefreshTokenRequest()) {
            tokenRequestBuilder.scope(AIS);
        }

        TokenRequest tokenRequest = tokenRequestBuilder.build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                                urlFactory.getOauth2TokenUrl(),
                                requestTime,
                                getTokenFromStorage(),
                                apiLogicFlowConfigurator.shouldSentTokenInRefreshAndExchangeToken())
                        .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TokenResponse.class, PolishApiErrorHandler.RequestType.POST);
    }

    @Override
    public TokenResponse exchangeTokenForAis(String refreshToken) {
        ZonedDateTime requestTime = getNow();
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();
        TokenRequest tokenRequest =
                TokenRequest.builder()
                        .requestHeader(
                                getRequestHeaderEntity(
                                        requestTime,
                                        getAccessTokenFromStorage(),
                                        apiLogicFlowConfigurator
                                                .shouldSentTokenInRefreshAndExchangeToken(),
                                        true))
                        .scopeDetails(getScopeDetailsForExchangeAisToken())
                        .clientId(apiConfiguration.getApiKey())
                        .scope(AIS)
                        .grantType(EXCHANGE_TOKEN)
                        .userAgent(PSU_USER_AGENT_VAL)
                        .userIp(getOriginatingUserIp())
                        .isUserSession(isUserPresent())
                        .exchangeToken(refreshToken)
                        .build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                                urlFactory.getOauth2TokenUrl(),
                                requestTime,
                                getTokenFromStorage(),
                                apiLogicFlowConfigurator.shouldSentTokenInRefreshAndExchangeToken())
                        .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TokenResponse.class, PolishApiErrorHandler.RequestType.POST);
    }

    private ScopeDetailsEntity getScopeDetailsForExchangeAisToken() {
        return ScopeDetailsEntity.builder()
                .privilegeList(prepareExchangeAisTokenRequestPrivilegeList())
                .consentId(getConsentIdForExchangeAisToken())
                .scopeTimeLimit(getScopeTimeLimit(CONSENT_LENGTH))
                .throttlingPolicy(THROTTLING_POLICY)
                .scopeGroupType(AIS)
                .build();
    }

    private String getConsentIdForExchangeAisToken() {
        if (apiLogicFlowConfigurator.shouldGenerateNewConsentIdInExchangeToken()) {
            String consentId = getUuid();
            persistentStorage.persistConsentId(consentId);
        }
        return persistentStorage.getConsentId();
    }

    private List<PrivilegeListEntity> prepareExchangeAisTokenRequestPrivilegeList() {
        List<PrivilegeListEntity> privilegeListEntities = new ArrayList<>();
        for (String accountNumber : persistentStorage.getAccountIdentifiers()) {
            privilegeListEntities.add(
                    PolishApiPostPrivilegeListEntityBuilder.getAisPrivilegesWithAccountNumber(
                            accountNumber, getMaxDaysToFetch()));
        }
        return privilegeListEntities;
    }
}
