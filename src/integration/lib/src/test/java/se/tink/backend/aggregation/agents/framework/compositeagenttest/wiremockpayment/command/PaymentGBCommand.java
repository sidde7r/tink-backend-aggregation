package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command;

import com.google.inject.Inject;
import java.util.List;
import javax.annotation.Nullable;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.verdict.GBPaymentVerdicter;
import se.tink.backend.aggregation.agents.payments.TypedPaymentControllerable;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.payment.rpc.Payment;

public class PaymentGBCommand implements CompositeAgentTestCommand {

    private final PaymentCommand paymentCommand;

    @Inject
    private PaymentGBCommand(
            Agent agent,
            AgentInstance agentInstance,
            SupplementalInformationController supplementalInformationController,
            @Nullable Payment payment,
            @Nullable List<Payment> bulkPayment)
            throws Exception {

        if (payment == null) {
            throw new IllegalArgumentException(
                    "Trying to execute a payment without supplying a payment.");
        }

        PaymentController paymentController =
                ((TypedPaymentControllerable) agent)
                        .getPaymentController(payment)
                        .orElseThrow(Exception::new);
        this.paymentCommand =
                new PaymentCommand(
                        agent,
                        agentInstance,
                        supplementalInformationController,
                        payment,
                        bulkPayment,
                        new GBPaymentVerdicter(paymentController));
    }

    @Override
    public void execute() throws Exception {
        paymentCommand.execute();
    }
}
