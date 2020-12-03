package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferCommand implements CompositeAgentTestCommand {
    private final Agent agent;
    private final Transfer transfer;

    @Inject
    public TransferCommand(Agent agent, Transfer transfer) {
        this.agent = agent;
        this.transfer = transfer;
    }

    @Override
    public void execute() throws Exception {
        if (agent instanceof TransferExecutor) {
            TransferExecutor transferExecutor = (TransferExecutor) agent;
            transferExecutor.execute(transfer);
        } else if (agent instanceof PaymentControllerable) {
            TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
            transferExecutorNxgen.execute(transfer);
        } else {
            throw new Exception("Not supported");
        }
    }
}
