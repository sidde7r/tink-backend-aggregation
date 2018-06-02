package se.tink.backend.aggregation.agents;

/**
 * @deprecated Use RefreshExecutor interface instead when implementing new agents
 */
@Deprecated
public interface DeprecatedRefreshExecutor {
    @Deprecated
    void refresh() throws Exception;
}
