package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.UnleashContext;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.HttpHeaders;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.strategies.aggregation.providersidsandexcludeappids.Constants;

@Slf4j
@RequiredArgsConstructor
public class FinancialApiHeaderFilter extends Filter {

    private static final String FEATURE_TOGGLE_NAME = "uk-fapi-interaction-header";
    private final String organisationId;
    private final String interactionId;
    private final AgentComponentProvider agentComponentProvider;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        httpRequest.getHeaders().putSingle(HttpHeaders.X_FAPI_FINANCIAL_ID, organisationId);
        if (featureToggleIsEnabled()) {
            httpRequest.getHeaders().putSingle(HttpHeaders.X_FAPI_INTERACTION_ID, interactionId);
            log.info(
                    "[FinancialApiHeaderFilter] Toggle status is enabled. "
                            + "Header x-fapi-interaction-id has been added to the request.");
        } else {
            log.info(
                    "[FinancialApiHeaderFilter] Toggle status is disabled. "
                            + "Header x-fapi-interaction-id has been skipped.");
        }
        return nextFilter(httpRequest);
    }

    private boolean featureToggleIsEnabled() {
        UnleashClient unleashClient = agentComponentProvider.getUnleashClient();
        Toggle toggle = getFeatureToggle();
        return unleashClient.isToggleEnabled(toggle);
    }

    private Toggle getFeatureToggle() {
        UnleashContext unleashContext = getUnleashContext();
        return Toggle.of(FEATURE_TOGGLE_NAME).context(unleashContext).build();
    }

    private UnleashContext getUnleashContext() {
        String credentialsId = getCredentialsId();
        CompositeAgentContext agentContext = agentComponentProvider.getContext();
        String providerName = Constants.Context.PROVIDER_NAME.name();
        String providerIdValue = agentContext.getProviderId();
        String appIdName = Constants.Context.APP_ID.name();
        String appIdValue = agentContext.getAppId();
        return UnleashContext.builder()
                .sessionId(credentialsId)
                .addProperty(providerName, providerIdValue)
                .addProperty(appIdName, appIdValue)
                .build();
    }

    private String getCredentialsId() {
        CredentialsRequest credentialsRequest = agentComponentProvider.getCredentialsRequest();
        Credentials credentials = credentialsRequest.getCredentials();
        return credentials.getId();
    }
}
