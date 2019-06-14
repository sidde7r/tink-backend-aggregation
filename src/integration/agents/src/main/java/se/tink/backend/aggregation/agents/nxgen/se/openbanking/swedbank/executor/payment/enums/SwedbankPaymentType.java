package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.enums;

import java.util.EnumMap;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentType;

public enum SwedbankPaymentType {
    SeDomesticCreditTransfers("se-domestic-credit-transfers"),
    SeInternationalCreditTransfers("se-international-credit-transfers"),
    Undefined("undefined");

    private String text;

    private static final EnumMap<SwedbankPaymentType, PaymentType> paymentTypeMapper =
            new EnumMap<>(SwedbankPaymentType.class);

    static {
        paymentTypeMapper.put(SeDomesticCreditTransfers, PaymentType.DOMESTIC);
        paymentTypeMapper.put(SeInternationalCreditTransfers, PaymentType.INTERNATIONAL);
        paymentTypeMapper.put(Undefined, PaymentType.UNDEFINED);
    }

    SwedbankPaymentType(String text) {
        this.text = text;
    }

    public static PaymentType mapToTinkPaymentType(SwedbankPaymentType swedbankPaymentType) {
        return Optional.ofNullable(paymentTypeMapper.get(swedbankPaymentType))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map payment product: "
                                                + swedbankPaymentType.toString()
                                                + " to Tink payment type"));
    }

    @Override
    public String toString() {
        return text;
    }
}
