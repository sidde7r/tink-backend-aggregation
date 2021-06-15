package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Filters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration.NordeaBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.filters.BankSideFailureFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.filters.BankSideRetryFilter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BadGatewayFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.RateLimitFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.BadGatewayRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

public abstract class NordeaBaseAgent extends NextGenerationAgent {
    protected NordeaBaseApiClient apiClient;
    protected String language;
    protected String providerMarket;

    public NordeaBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        this.providerMarket = componentProvider.getCredentialsRequest().getProvider().getMarket();

        try {
            this.language = request.getUser().getLocale().split("_")[0];
        } catch (RuntimeException e) {
            this.language = NordeaBaseConstants.QueryValues.DEFAULT_LANGUAGE;
        }
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setConfiguration(getAgentConfiguration());
        this.client.setEidasProxy(configuration.getEidasProxy());
        /* The agent is not supposed to follow the authorization redirect. The authorize redirect
        is between user (PSU) and the bank (ASPSP) handled in a third party app (browser) without
        TPP involvement */
        this.client.setFollowRedirects(false);
        this.client.addFilter(
                new TimeoutRetryFilter(
                        NordeaBaseConstants.Filters.NUMBER_OF_RETRIES,
                        NordeaBaseConstants.Filters.MS_TO_WAIT));
        this.client.addFilter(
                new BadGatewayRetryFilter(
                        NordeaBaseConstants.Filters.NUMBER_OF_RETRIES,
                        NordeaBaseConstants.Filters.MS_TO_WAIT));
        this.client.addFilter(
                new RateLimitFilter(
                        provider.getName(),
                        Filters.RATE_LIMIT_RETRY_MS_MIN,
                        Filters.RATE_LIMIT_RETRY_MS_MAX,
                        Filters.NUMBER_OF_RETRIES));
        this.client.addFilter(new BankSideFailureFilter());
        this.client.addFilter(new BankSideRetryFilter());
        this.client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        this.client.addFilter(new TimeoutFilter());
        this.client.addFilter(new BadGatewayFilter());
    }

    protected AgentConfiguration<NordeaBaseConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(NordeaBaseConfiguration.class);
    }

    @Override
    protected abstract Authenticator constructAuthenticator();

    @Override
    protected abstract SessionHandler constructSessionHandler();
}
