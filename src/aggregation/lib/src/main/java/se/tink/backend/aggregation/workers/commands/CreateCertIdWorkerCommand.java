package se.tink.backend.aggregation.workers.commands;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.eidasidentity.CertificateIdProvider;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

@RequiredArgsConstructor
public class CreateCertIdWorkerCommand extends AgentWorkerCommand {
    private final AgentWorkerCommandContext context;
    private final CertificateIdProvider certificateIdProvider;

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        Provider provider = context.getRequest().getProvider();
        context.setCertId(
                certificateIdProvider.getCertId(
                        context.getAppId(),
                        context.getClusterId(),
                        provider.getName(),
                        provider.getMarket()));
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // no action
    }
}
