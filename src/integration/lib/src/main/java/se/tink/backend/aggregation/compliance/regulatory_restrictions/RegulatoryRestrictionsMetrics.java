package se.tink.backend.aggregation.compliance.regulatory_restrictions;

import com.google.inject.Inject;
import javax.annotation.Nullable;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

class RegulatoryRestrictionsMetrics {
    private static final MetricId regulatoryRestriction =
            MetricId.newId("regulatory_restrictions_accounts");
    private final MetricRegistry metricRegistry;

    @Inject
    RegulatoryRestrictionsMetrics(@Nullable MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    void recordAccountRestrictionDecision(
            Provider provider, Account account, boolean isRestricted) {
        metricRegistry
                .meter(
                        regulatoryRestriction
                                .label("provider", provider.getName())
                                .label("market", provider.getMarket())
                                .label("account_type", account.getType().name())
                                .label("is_restricted", isRestricted))
                .inc();
    }
}
