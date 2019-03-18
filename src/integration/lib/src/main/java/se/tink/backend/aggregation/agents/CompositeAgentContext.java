package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.FinancialDataCacher;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;


// Temporary interface until we decompose completely
public interface CompositeAgentContext extends
        FinancialDataCacher,
        StatusUpdater,
        SupplementalRequester,
        SystemUpdater,
        AgentAggregatorIdentifier,
        MetricContext {

    void clear();

}
