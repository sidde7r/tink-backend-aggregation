package se.tink.backend.aggregation.workers.commands.state;

import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.mysql.aggregation.aggregationcredentials.AggregationCredentialsRepository;

public class EncryptAgentWorkerCommandState {
    private AggregationCredentialsRepository aggregationCredentialsRepository;

    public EncryptAgentWorkerCommandState(ServiceContext serviceContext) {
        aggregationCredentialsRepository = serviceContext.getRepository(AggregationCredentialsRepository.class);
    }

    public AggregationCredentialsRepository getAggregationCredentialsRepository() {
        return aggregationCredentialsRepository;
    }
}