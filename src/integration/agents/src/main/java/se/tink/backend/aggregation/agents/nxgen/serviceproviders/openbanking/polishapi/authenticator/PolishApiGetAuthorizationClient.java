package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.GrantTypes.AUTHORIZATION_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.GrantTypes.EXCHANGE_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.GrantTypes.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.SCOPE_USAGE_LIMIT_MULTIPLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.GetClient.AIS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.GetClient.AIS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.GetClient.THROTTLING_POLICY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Localization.DATE_TIME_FORMATTER_REQUEST_HEADERS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions.SUPPORTED_TRANSACTION_TYPES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.common.ScopeDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.authorize.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeItemWithHistoryAndTransactionStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.token.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.BasePolishApiGetClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents.PolishApiAgentCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrorHandler;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class PolishApiGetAuthorizationClient extends BasePolishApiGetClient
        implements PolishApiAuthorizationClient {

    private final PolishAuthorizeApiUrlFactory urlFactory;
    private final Integer maxDaysToFetch;

    public PolishApiGetAuthorizationClient(
            PolishApiAgentCreator apiAgentCreator,
            TinkHttpClient httpClient,
            AgentConfiguration<PolishApiConfiguration> configuration,
            AgentComponentProvider agentComponentProvider,
            PolishApiPersistentStorage persistentStorage) {
        super(
                httpClient,
                apiAgentCreator,
                configuration,
                agentComponentProvider,
                persistentStorage);
        this.urlFactory = apiAgentCreator.getAuthorizeApiUrlFactory();
        this.maxDaysToFetch = apiAgentCreator.getMaxDaysToFetch();
    }

    @Override
    public URL getAuthorizeUrl(String state) {
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();
        String consentId = getUuid();
        persistentStorage.persistConsentId(consentId);

        AuthorizeRequest authorizeRequest =
                AuthorizeRequest.builder()
                        .clientId(apiConfiguration.getApiKey())
                        .redirectUri(configuration.getRedirectUrl())
                        .responseType(CODE)
                        .scope(PolishApiConstants.Authorization.Common.Scopes.AIS_ACCOUNTS)
                        .scopeDetails(
                                ScopeDetailsEntity.builder()
                                        .consentId(consentId)
                                        .privilegeList(prepareAisAccountPrivilegeList())
                                        .scopeTimeDuration(89)
                                        .scopeTimeLimit(
                                                getNow().plusDays(89)
                                                        .format(
                                                                DATE_TIME_FORMATTER_REQUEST_HEADERS))
                                        .throttlingPolicy(THROTTLING_POLICY)
                                        .scopeGroupType(AIS_ACCOUNTS)
                                        .build())
                        .state(state)
                        .build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(urlFactory.getAuthorizeUrl(), null)
                        .body(authorizeRequest, MediaType.APPLICATION_JSON);

        AuthorizationResponse authorizationResponse =
                PolishApiErrorHandler.callWithErrorHandling(
                        requestBuilder,
                        AuthorizationResponse.class,
                        PolishApiErrorHandler.RequestType.POST);

        return new URL(authorizationResponse.getRedirectUri());
    }

    private List<PrivilegeListEntity> prepareAisAccountPrivilegeList() {
        PrivilegeListEntity privilegeListEntity =
                PrivilegeListEntity.builder()
                        .aisAccountsAccounts(
                                PrivilegeItemEntity.builder()
                                        .scopeUsageLimit(SCOPE_USAGE_LIMIT_MULTIPLE.toUpperCase())
                                        .build())
                        .build();

        return Arrays.asList(privilegeListEntity);
    }

    @Override
    public TokenResponse exchangeAuthorizationToken(String accessCode) {
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();

        TokenRequest.TokenRequestBuilder<?, ?> tokenRequestBuilder =
                TokenRequest.builder()
                        .clientId(apiConfiguration.getApiKey())
                        .grantType(AUTHORIZATION_CODE)
                        .redirectUri(configuration.getRedirectUrl());

        setAuthorizationCode(accessCode, tokenRequestBuilder);

        TokenRequest tokenRequest = tokenRequestBuilder.build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(urlFactory.getOauth2TokenUrl(), null)
                        .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TokenResponse.class, PolishApiErrorHandler.RequestType.POST);
    }

    private void setAuthorizationCode(
            String accessCode, TokenRequest.TokenRequestBuilder<?, ?> tokenRequestBuilder) {
        if (polishApiAgentCreator.shouldSentAuthorizationCodeInUpperCaseField()) {
            tokenRequestBuilder.codeUpperCase(accessCode);
        } else {
            tokenRequestBuilder.codeLowerCase(accessCode);
        }
    }

    @Override
    public TokenResponse exchangeRefreshToken(String refreshToken) {
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();

        TokenRequest tokenRequest =
                TokenRequest.builder()
                        .clientId(apiConfiguration.getApiKey())
                        .grantType(REFRESH_TOKEN)
                        .refreshToken(refreshToken)
                        .redirectUri(configuration.getRedirectUrl())
                        .build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(urlFactory.getOauth2TokenUrl(), getTokenFromStorage())
                        .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TokenResponse.class, PolishApiErrorHandler.RequestType.POST);
    }

    @Override
    public TokenResponse exchangeTokenForAis(String refreshToken) {
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();

        TokenRequest tokenRequest =
                TokenRequest.builder()
                        .scope(PolishApiConstants.Authorization.Common.Scopes.AIS)
                        .scopeDetails(
                                ScopeDetailsEntity.builder()
                                        .consentId(persistentStorage.getConsentId())
                                        .privilegeList(prepareAisPrivilegeList())
                                        .scopeTimeDuration(89)
                                        .scopeTimeLimit(
                                                getNow().plusDays(89)
                                                        .format(
                                                                DATE_TIME_FORMATTER_REQUEST_HEADERS))
                                        .throttlingPolicy(THROTTLING_POLICY)
                                        .scopeGroupType(AIS)
                                        .build())
                        .clientId(apiConfiguration.getApiKey())
                        .grantType(EXCHANGE_TOKEN)
                        .exchangeToken(refreshToken)
                        .redirectUri(configuration.getRedirectUrl())
                        .build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(urlFactory.getOauth2TokenUrl(), getTokenFromStorage())
                        .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TokenResponse.class, PolishApiErrorHandler.RequestType.POST);
    }

    private List<PrivilegeListEntity> prepareAisPrivilegeList() {
        List<PrivilegeListEntity> privilegeListEntities = new ArrayList<>();
        for (String accountNumber : persistentStorage.getAccountIdentifiers()) {
            privilegeListEntities.add(
                    PrivilegeListEntity.builder()
                            .accountNumber(Arrays.asList(accountNumber))
                            .aisTransactions(
                                    PrivilegeItemWithHistoryAndTransactionStatusEntity.builder()
                                            .scopeUsageLimit(
                                                    SCOPE_USAGE_LIMIT_MULTIPLE.toUpperCase())
                                            .maxAllowedHistoryLong(maxDaysToFetch)
                                            .transactionStatus(
                                                    SUPPORTED_TRANSACTION_TYPES.stream()
                                                            .map(Enum::name)
                                                            .collect(Collectors.toList()))
                                            .build())
                            .aisAccountDetails(
                                    PrivilegeItemEntity.builder()
                                            .scopeUsageLimit(
                                                    SCOPE_USAGE_LIMIT_MULTIPLE.toUpperCase())
                                            .build())
                            .build());
        }

        return privilegeListEntities;
    }
}
