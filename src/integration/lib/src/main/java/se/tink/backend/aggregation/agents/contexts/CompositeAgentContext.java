package se.tink.backend.aggregation.agents.contexts;

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
                LoggingContext,
                EidasContext,
                TestContext,
                UnleashContext,
                AgentTemporaryStorageContext,
                RawBankDataEventContext,
                CorrelationIdentifierContext {}
