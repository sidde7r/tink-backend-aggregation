package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums;

import static se.tink.libraries.payments.common.model.PaymentScheme.INSTANT_NORWEGIAN_DOMESTIC_CREDIT_TRANSFER_STRAKS;
import static se.tink.libraries.payments.common.model.PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER;
import static se.tink.libraries.transfer.rpc.PaymentServiceType.PERIODIC;
import static se.tink.libraries.transfer.rpc.PaymentServiceType.SINGLE;

import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public enum DnbPaymentType {
    NORWEGIAN_DOMESTIC_CREDIT_TRANSFERS(
            "payments", "norwegian-domestic-credit-transfers", PaymentType.DOMESTIC),
    NORWEGIAN_DOMESTIC_CREDIT_TRANSFERS_PERIODIC(
            "periodic-payments", "norwegian-domestic-credit-transfers", PaymentType.DOMESTIC),
    NORWEGIAN_DOMESTIC_CREDIT_TRANSFERS_INSTANT(
            "payments", "instant-norwegian-domestic-credit-transfers-straks", PaymentType.DOMESTIC);

    private final String typePath;
    private final String subtypePath;
    private final PaymentType paymentType;

    DnbPaymentType(String typePath, String subtypePath, PaymentType paymentType) {
        this.typePath = typePath;
        this.subtypePath = subtypePath;
        this.paymentType = paymentType;
    }

    public static DnbPaymentType getDnbPaymentType(PaymentRequest paymentRequest) {
        PaymentScheme paymentScheme = paymentRequest.getPayment().getPaymentScheme();
        PaymentServiceType paymentServiceType = paymentRequest.getPayment().getPaymentServiceType();

        if (paymentScheme == NORWEGIAN_DOMESTIC_CREDIT_TRANSFER && paymentServiceType == PERIODIC) {
            return NORWEGIAN_DOMESTIC_CREDIT_TRANSFERS_PERIODIC;
        } else if (paymentScheme == NORWEGIAN_DOMESTIC_CREDIT_TRANSFER
                && paymentServiceType == SINGLE) {
            return NORWEGIAN_DOMESTIC_CREDIT_TRANSFERS;
        } else if (paymentScheme == INSTANT_NORWEGIAN_DOMESTIC_CREDIT_TRANSFER_STRAKS) {
            return NORWEGIAN_DOMESTIC_CREDIT_TRANSFERS_INSTANT;
        } else {
            throw new UnsupportedOperationException(
                    String.format(
                            "Unsupported payment type, paymentScheme: %s, paymentServiceType: %s",
                            paymentScheme, paymentServiceType));
        }
    }

    public String getTypePath() {
        return typePath;
    }

    public String getSubtypePath() {
        return subtypePath;
    }

    public PaymentType getTinkPaymentType() {
        return paymentType;
    }
}
