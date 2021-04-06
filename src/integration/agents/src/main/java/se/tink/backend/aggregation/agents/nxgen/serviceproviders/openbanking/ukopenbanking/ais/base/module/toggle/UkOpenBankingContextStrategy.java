package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import java.util.List;
import java.util.Map;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.strategy.Strategy;

class UkOpenBankingContextStrategy implements Strategy {
    private final String strategyName;

    public UkOpenBankingContextStrategy(String strategyName) {
        this.strategyName = strategyName;
    }

    @Override
    public String getName() {
        return strategyName;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        UkOpenBankingStrategyProperties ukOpenBankingStrategyProperties =
                new UkOpenBankingStrategyProperties(parameters);
        AgentRegisteredProperties agentRegisteredProperties =
                new AgentRegisteredProperties(unleashContext.getProperties());

        return areProvidersNamesMatch(
                        ukOpenBankingStrategyProperties.getProvidersNames(),
                        agentRegisteredProperties.getCurrentProviderName())
                && isAppIdAllowed(
                        agentRegisteredProperties.getCurrentAppId(),
                        ukOpenBankingStrategyProperties.getExcludedAppIds());
    }

    private boolean areProvidersNamesMatch(
            List<String> allowedProvidersNames, String currentProviderName) {
        return allowedProvidersNames.contains(currentProviderName);
    }

    private boolean isAppIdAllowed(String currentAppId, List<String> excludedAppIds) {
        return !excludedAppIds.contains(currentAppId);
    }
}
