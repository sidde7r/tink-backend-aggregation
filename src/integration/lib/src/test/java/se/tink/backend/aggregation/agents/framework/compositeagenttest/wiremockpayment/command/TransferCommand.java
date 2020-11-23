package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command;

import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferCommand implements CompositeAgentTestCommand {
    private final Agent agent;
    private final List<Transfer> transferList;

    @Inject
    public TransferCommand(Agent agent, List<Transfer> transfers) {
        this.agent = agent;
        this.transferList = transfers;
    }

    @Override
    public void execute() throws Exception {
        if (agent instanceof TransferExecutor) {
            TransferExecutor transferExecutor = (TransferExecutor) agent;
            transferExecutorExecute(transferExecutor);
        } else if (agent instanceof PaymentControllerable) {
            TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
            transferExecutorNxgenExecute(transferExecutorNxgen);
        } else {
            throw new Exception("Not supported");
        }
    }

    private void transferExecutorExecute(TransferExecutor transferExecutor) throws Exception {
        for (Transfer transfer : transferList) {
            transferExecutor.execute(transfer);
        }
    }

    private void transferExecutorNxgenExecute(TransferExecutorNxgen transferExecutorNxgen) {
        for (Transfer transfer : transferList) {
            transferExecutorNxgen.execute(transfer);
        }
    }
}
