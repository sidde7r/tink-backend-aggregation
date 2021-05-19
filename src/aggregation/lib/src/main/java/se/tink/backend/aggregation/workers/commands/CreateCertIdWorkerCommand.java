package se.tink.backend.aggregation.workers.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.eidasidentity.CertificateIdProvider;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

@RequiredArgsConstructor
@Slf4j
public class CreateCertIdWorkerCommand extends AgentWorkerCommand {
    private final AgentWorkerCommandContext context;
    private final CertificateIdProvider certificateIdProvider;

    @Override
    protected AgentWorkerCommandResult doExecute() {
        try {
            Provider provider = context.getRequest().getProvider();
            context.setCertId(
                    certificateIdProvider.getCertId(
                            context.getAppId(),
                            context.getClusterId(),
                            provider.getName(),
                            provider.getMarket(),
                            provider.isOpenBanking()));
        } catch (Exception e) {
            log.warn("Could not fetch certId", e);
            context.setCertId("DEFAULT");
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // no action
    }
}
