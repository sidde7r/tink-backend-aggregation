package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.AgentConfigurationControllerContext;
import se.tink.backend.aggregation.agents.contexts.FinancialDataCacher;
import se.tink.backend.aggregation.agents.contexts.LogMaskable;
import se.tink.backend.aggregation.agents.contexts.LogOutputStreamable;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;

// Temporary interface until we decompose completely
public interface CompositeAgentContext
        extends FinancialDataCacher,
                StatusUpdater,
                SupplementalRequester,
                ProviderSessionCacheContext,
                SystemUpdater,
                AgentAggregatorIdentifier,
                MetricContext,
                LogMaskable,
                AgentConfigurationControllerContext,
                LogOutputStreamable {

    void clear();
}
