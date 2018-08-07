package se.tink.backend.aggregation.workers.commands.state;

import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.mysql.aggregation.aggregationcredentials.AggregationCredentialsRepository;

public class DeleteAgentWorkerCommandState {
    private AggregationCredentialsRepository aggregationCredentialsRepository;

    public DeleteAgentWorkerCommandState(ServiceContext serviceContext) {
        this.aggregationCredentialsRepository = serviceContext
                .getRepository(AggregationCredentialsRepository.class);
    }

    public AggregationCredentialsRepository getAggregationCredentialsRepository() {
        return aggregationCredentialsRepository;
    }
}
