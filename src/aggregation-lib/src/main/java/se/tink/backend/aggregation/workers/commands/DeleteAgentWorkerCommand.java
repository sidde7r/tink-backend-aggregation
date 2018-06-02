package se.tink.backend.aggregation.workers.commands;

import org.springframework.dao.EmptyResultDataAccessException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.mysql.aggregation.AggregationCredentialsRepository;

@Deprecated
public class DeleteAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger log = new AggregationLogger(DeleteAgentWorkerCommand.class);

    public static class DeleteAgentWorkerCommandState {
        private AggregationCredentialsRepository aggregationCredentialsRepository;

        public DeleteAgentWorkerCommandState(ServiceContext serviceContext) {
            this.aggregationCredentialsRepository = serviceContext
                    .getRepository(AggregationCredentialsRepository.class);
        }

        public AggregationCredentialsRepository getAggregationCredentialsRepository() {
            return aggregationCredentialsRepository;
        }
    }

    private DeleteAgentWorkerCommandState state;
    private AgentWorkerContext context;

    public DeleteAgentWorkerCommand(AgentWorkerContext context, DeleteAgentWorkerCommandState state) {
        this.context = context;
        this.state = state;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        try {
            state.getAggregationCredentialsRepository().delete(context.getRequest().getCredentials().getId());
        } catch (EmptyResultDataAccessException e) {
            log.info("Credentials has already been deleted.");
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
