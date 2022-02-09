package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command;

import com.google.inject.Inject;
import java.util.List;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.payments.PaymentControllerable;
import se.tink.backend.aggregation.agents.payments.TransferExecutor;
import se.tink.backend.aggregation.agents.payments.TransferExecutorNxgen;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.transfer.rpc.Transfer;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.BulkPaymentInitiation;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationReport;

@Slf4j
public class TransferCommand implements CompositeAgentTestCommand {
    private final AgentInstance agentInstance;
    private final Agent agent;
    private final SupplementalInformationController supplementalInformationController;
    @Nullable private final Transfer transfer;
    @Nullable private final List<Transfer> bulkTransfer;

    @Inject
    public TransferCommand(
            AgentInstance agentInstance,
            Agent agent,
            SupplementalInformationController supplementalInformationController,
            @Nullable Transfer transfer,
            @Nullable List<Transfer> bulkTransfer) {
        this.agentInstance = agentInstance;
        this.agent = agent;
        this.supplementalInformationController = supplementalInformationController;
        this.transfer = transfer;
        this.bulkTransfer = bulkTransfer;
    }

    @Override
    public void execute() throws Exception {
        if (bulkTransfer != null) {
            if (agentInstance.supportsBulkPaymentInitiation()) {
                BulkPaymentInitiation bulkPaymentInitiation =
                        new BulkPaymentInitiation(supplementalInformationController, agentInstance);

                PaymentInitiationReport paymentInitiationReport =
                        bulkPaymentInitiation.initiateBulkPaymentsWithRpcTransfers(bulkTransfer);
                log.info(
                        "Final payment states: {}",
                        paymentInitiationReport.getFinalPaymentStates());
            } else {
                throw new Exception("Not supported");
            }
        } else if (transfer != null) {
            if (agent instanceof TransferExecutor) {
                TransferExecutor transferExecutor = (TransferExecutor) agent;
                transferExecutor.execute(transfer);
            } else if (agent instanceof PaymentControllerable) {
                TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
                transferExecutorNxgen.execute(transfer);
            } else {
                throw new Exception("Not supported");
            }
        } else {
            throw new IllegalArgumentException(
                    "Trying to execute a transfer without supplying a transfer or a bulkTransfer.");
        }
    }
}
