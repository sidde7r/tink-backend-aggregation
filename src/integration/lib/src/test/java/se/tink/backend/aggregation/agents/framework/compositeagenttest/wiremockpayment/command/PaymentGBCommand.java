package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command;

import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.verdict.GBPaymentVerdicter;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.payment.rpc.Payment;

public class PaymentGBCommand implements CompositeAgentTestCommand {

    private final PaymentCommand paymentCommand;

    @Inject
    private PaymentGBCommand(Agent agent, List<Payment> paymentList) throws Exception {
        PaymentController paymentController =
                ((PaymentControllerable) agent).getPaymentController().orElseThrow(Exception::new);
        this.paymentCommand =
                new PaymentCommand(agent, paymentList, new GBPaymentVerdicter(paymentController));
    }

    @Override
    public void execute() throws Exception {
        paymentCommand.execute();
    }
}
