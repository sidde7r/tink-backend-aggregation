package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpm.payment;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobePaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.enums.CbiGlobePaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.payment.enums.PaymentStatus;

@Slf4j
public class BpmPaymentExecutor extends CbiGlobePaymentExecutor {

    public BpmPaymentExecutor(
            CbiGlobePaymentApiClient paymentApiClient,
            SupplementalInformationController supplementalInformationController,
            CbiStorage storage,
            CbiGlobePaymentRequestBuilder paymentRequestBuilder) {
        super(paymentApiClient, supplementalInformationController, storage, paymentRequestBuilder);
    }

    @Override
    protected boolean checkIfPaymentInIntermediateStatusIsSuccessful(
            CreatePaymentResponse paymentStatusResponse) {
        // As for BPM payment is parked for 30 min at bank in RCVD state we need special handling.
        // Ref: https://tinkab.atlassian.net/browse/PAY2-734
        return paymentStatusResponse.getLinks() == null
                && checkIfPaymentIsSuccesful(paymentStatusResponse);
    }

    @Override
    protected PaymentMultiStepResponse buildSuccessfulPaymentResponse(
            PaymentMultiStepRequest paymentRequest, CreatePaymentResponse paymentStatusResponse) {
        if (CbiGlobePaymentStatus.RCVD
                == CbiGlobePaymentStatus.fromString(paymentStatusResponse.getTransactionStatus())) {
            log.info("BPM special behaviour - marking RCVD payment as signed.");
            paymentRequest.getPayment().setStatus(PaymentStatus.SIGNED);
        }

        return super.buildSuccessfulPaymentResponse(paymentRequest, paymentStatusResponse);
    }
}
