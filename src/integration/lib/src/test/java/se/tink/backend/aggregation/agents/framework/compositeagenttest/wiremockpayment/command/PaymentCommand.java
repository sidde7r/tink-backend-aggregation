package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.PaymentControllerable;
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
    private final List<Payment> paymentList;
    private final PaymentVerdict paymentVerdict;

    @Inject
    PaymentCommand(Agent agent, List<Payment> paymentList, PaymentVerdict paymentVerdict) {
        this.agent = agent;
        this.paymentList = paymentList;
        this.paymentVerdict = paymentVerdict;
    }

    @Override
    public void execute() throws Exception {
        PaymentController paymentController =
                ((PaymentControllerable) agent).getPaymentController().orElseThrow(Exception::new);

        for (Payment payment : paymentList) {

            PaymentResponse createPaymentResponse =
                    paymentController.create(new PaymentRequest(payment));

            Storage storage = Storage.copyOf(createPaymentResponse.getStorage());

            PaymentMultiStepResponse signPaymentMultiStepResponse =
                    paymentController.sign(PaymentMultiStepRequest.of(createPaymentResponse));

            Map<String, String> map;
            List<Field> fields;
            String nextStep = signPaymentMultiStepResponse.getStep();
            Payment paymentSign = signPaymentMultiStepResponse.getPayment();
            Storage storageSign = signPaymentMultiStepResponse.getStorage();

            while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
                fields = signPaymentMultiStepResponse.getFields();
                map = Collections.emptyMap();

                signPaymentMultiStepResponse =
                        paymentController.sign(
                                new PaymentMultiStepRequest(
                                        payment,
                                        storage,
                                        nextStep,
                                        fields,
                                        new ArrayList<>(map.values())));
                nextStep = signPaymentMultiStepResponse.getStep();
                paymentSign = signPaymentMultiStepResponse.getPayment();
                storageSign = signPaymentMultiStepResponse.getStorage();
            }
            paymentVerdict.verdictOnPaymentStatus(signPaymentMultiStepResponse);
        }
    }
}
