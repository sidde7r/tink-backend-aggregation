package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.verdict.PaymentVerdict;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;

public final class PaymentCommand implements CompositeAgentTestCommand {

    private final Agent agent;
    private final Payment payment;
    private final PaymentVerdict paymentVerdict;

    @Inject
    PaymentCommand(Agent agent, Payment payment, PaymentVerdict paymentVerdict) {
        this.agent = agent;
        this.payment = payment;
        this.paymentVerdict = paymentVerdict;
    }

    @Override
    public void execute() throws Exception {
        PaymentController paymentController;
        if (agent instanceof TypedPaymentControllerable) {
            paymentController =
                    ((TypedPaymentControllerable) agent)
                            .getPaymentController(payment)
                            .orElseThrow(Exception::new);

        } else {
            paymentController =
                    ((PaymentControllerable) agent)
                            .getPaymentController()
                            .orElseThrow(Exception::new);
        }

        PaymentResponse createPaymentResponse =
                paymentController.create(new PaymentRequest(payment));

        Storage storage = Storage.copyOf(createPaymentResponse.getStorage());

        PaymentMultiStepResponse signPaymentMultiStepResponse =
                paymentController.sign(PaymentMultiStepRequest.of(createPaymentResponse));

        Map<String, String> map;
        List<Field> fields;
        String nextStep = signPaymentMultiStepResponse.getStep();
        Payment paymentResponse = signPaymentMultiStepResponse.getPayment();

        while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
            fields = signPaymentMultiStepResponse.getFields();
            map = Collections.emptyMap();

            signPaymentMultiStepResponse =
                    paymentController.sign(
                            new PaymentMultiStepRequest(
                                    paymentResponse,
                                    storage,
                                    nextStep,
                                    fields,
                                    new ArrayList<>(map.values())));
            nextStep = signPaymentMultiStepResponse.getStep();
            paymentResponse = signPaymentMultiStepResponse.getPayment();
            storage = signPaymentMultiStepResponse.getStorage();
        }
        paymentVerdict.verdictOnPaymentStatus(signPaymentMultiStepResponse);
    }
}
