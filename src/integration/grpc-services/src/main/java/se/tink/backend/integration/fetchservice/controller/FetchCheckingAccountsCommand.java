package se.tink.backend.integration.fetchservice.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class FetchCheckingAccountsCommand {
    private String operationId;
    private AgentInfo agentInfo;
    private Credentials credentials;
    private AggregatorInfo aggregatorInfo;

    FetchCheckingAccountsCommand(
            String operationId,
            AgentInfo agentInfo,
            Credentials credentials,
            AggregatorInfo aggregatorInfo) {
        this.operationId = operationId;
        this.agentInfo = agentInfo;
        this.credentials = credentials;
        this.aggregatorInfo = aggregatorInfo;
    }

    public static FetchCheckingAccountsCommand of(
            String operationId,
            AgentInfo agentInfo,
            Credentials credentials,
            AggregatorInfo aggregatorInfo) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(operationId));
        Preconditions.checkNotNull(agentInfo);
        Preconditions.checkNotNull(credentials);
        Preconditions.checkNotNull(aggregatorInfo);

        // FIXME: add more elaborate checks per command
        return new FetchCheckingAccountsCommand(
                operationId, agentInfo, credentials, aggregatorInfo);
    }

    public String getOperationId() {
        return operationId;
    }

    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public AggregatorInfo getAggregatorInfo() {
        return aggregatorInfo;
    }
}
