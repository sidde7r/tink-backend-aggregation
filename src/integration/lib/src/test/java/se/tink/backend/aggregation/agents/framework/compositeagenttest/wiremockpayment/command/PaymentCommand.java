package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.verdict.PaymentVerdict;
import se.tink.backend.aggregation.agents.payments.PaymentControllerable;
import se.tink.backend.aggregation.agents.payments.TypedPaymentControllerable;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.BulkPaymentInitiation;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationReport;

@Slf4j
public final class PaymentCommand implements CompositeAgentTestCommand {

    private final Agent agent;
    private final AgentInstance agentInstance;
    private final SupplementalInformationController supplementalInformationController;
    @Nullable private final Payment payment;
    @Nullable private final List<Payment> bulkPayment;
    private final PaymentVerdict paymentVerdict;

    @Inject
    PaymentCommand(
            Agent agent,
            AgentInstance agentInstance,
            SupplementalInformationController supplementalInformationController,
            @Nullable Payment payment,
            @Nullable List<Payment> bulkPayment,
            PaymentVerdict paymentVerdict) {
        this.agent = agent;
        this.agentInstance = agentInstance;
        this.supplementalInformationController = supplementalInformationController;
        this.payment = payment;
        this.bulkPayment = bulkPayment;
        this.paymentVerdict = paymentVerdict;
    }

    @Override
    public void execute() throws Exception {
        if (bulkPayment != null) {
            tryExecuteBulkPayment();
        } else if (payment != null) {
            tryExecuteSinglePayment();
        } else {
            throw new IllegalArgumentException(
                    "Trying to execute a payment without supplying a payment or a bulkPayment.");
        }
    }

    private void tryExecuteBulkPayment() throws Exception {
        if (!agentInstance.supportsBulkPaymentInitiation()) {
            throw new Exception("Not supported");
        }

        BulkPaymentInitiation bulkPaymentInitiation =
                new BulkPaymentInitiation(supplementalInformationController, agentInstance);

        PaymentInitiationReport paymentInitiationReport =
                bulkPaymentInitiation.initiateBulkPaymentsWithRpcPayments(bulkPayment);
        log.info("Final payment states: {}", paymentInitiationReport.getFinalPaymentStates());
    }

    private void tryExecuteSinglePayment() throws Exception {
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
        String nextStep = signPaymentMultiStepResponse.getStep();
        Payment paymentResponse = signPaymentMultiStepResponse.getPayment();

        while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
            map = Collections.emptyMap();

            signPaymentMultiStepResponse =
                    paymentController.sign(
                            new PaymentMultiStepRequest(
                                    paymentResponse,
                                    storage,
                                    nextStep,
                                    new ArrayList<>(map.values())));
            nextStep = signPaymentMultiStepResponse.getStep();
            paymentResponse = signPaymentMultiStepResponse.getPayment();
            storage = signPaymentMultiStepResponse.getStorage();
        }
        paymentVerdict.verdictOnPaymentStatus(signPaymentMultiStepResponse);
    }
}
