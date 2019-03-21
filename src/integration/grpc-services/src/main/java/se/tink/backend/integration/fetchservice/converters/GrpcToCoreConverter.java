package se.tink.backend.integration.fetchservice.converters;

import se.tink.backend.integration.api.rpc.IntegrationRequest;
import se.tink.backend.integration.fetchservice.controller.AgentInfo;
import se.tink.backend.integration.fetchservice.controller.AggregatorInfo;
import se.tink.backend.integration.fetchservice.controller.Credentials;
import se.tink.backend.integration.fetchservice.controller.FetchCheckingAccountsCommand;


public class GrpcToCoreConverter {

    public static FetchCheckingAccountsCommand convert(IntegrationRequest request) {

        return FetchCheckingAccountsCommand.of(
                request.getOperationId(),
                AgentInfo.of(request.getAgentInfo().getAgentClassName(), request.getAgentInfo().getState().getState()),
                Credentials.of(
                        request.getCredentials().getId(),
                        request.getCredentials().getUserId(),
                        request.getCredentials().getFieldsSerialized(),
                        EnumMapper.CREDENTIALS_TYPE.getOrDefault(request.getCredentials().getType(), Credentials.Type.UNKNOWN)),
                AggregatorInfo.of(request.getAggregatorInfo().getClientId(),
                        request.getAggregatorInfo().getAggregatorIdentifier()));

    }
}
