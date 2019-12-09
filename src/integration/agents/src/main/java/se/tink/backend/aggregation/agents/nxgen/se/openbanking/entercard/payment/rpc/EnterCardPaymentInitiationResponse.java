package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

@JsonObject
public class EnterCardPaymentInitiationResponse {

    private PaymentInitiationResponse paymentInitiationResponse;

    public PaymentInitiationResponse getPaymentInitiationResponse() {
        return paymentInitiationResponse;
    }

    public void setPaymentInitiationResponse(PaymentInitiationResponse paymentInitiationResponse) {
        this.paymentInitiationResponse = paymentInitiationResponse;
    }

    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest, String state) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(paymentRequest.getPayment().getCreditor())
                        .withDebtor(paymentRequest.getPayment().getDebtor())
                        .withAmount(
                                new Amount(
                                        paymentRequest.getPayment().getCurrency(),
                                        paymentRequest.getPayment().getAmount().getValue()))
                        .withExecutionDate(null)
                        .withCurrency(paymentRequest.getPayment().getCurrency())
                        .withUniqueId(paymentRequest.getPayment().getUniqueId())
                        .withStatus(PaymentStatus.PENDING)
                        .withType(PaymentType.DOMESTIC)
                        .withReference(
                                new Reference(
                                        null, paymentInitiationResponse.getPaymentReferenceId()));

        Storage storage = paymentRequest.getStorage();
        storage.put(StorageKeys.E_SIGN_URL, paymentInitiationResponse.geteSignUrl());
        storage.put(StorageKeys.STATE, state);

        return new PaymentResponse(buildingPaymentResponse.build(), storage);
    }
}
