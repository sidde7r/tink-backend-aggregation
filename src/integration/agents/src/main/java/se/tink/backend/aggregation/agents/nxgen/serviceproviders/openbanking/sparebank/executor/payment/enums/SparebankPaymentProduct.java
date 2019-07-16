package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentType;

public enum SparebankPaymentProduct {
    NORWEGIAN_DOMESTIC_CREDIT_TRANSFER("norwegian-domestic-credit-transfers"),
    CROSS_BORDER_CREDIT_TRANSFER("cross-border-credit-transfers"),
    SEPA_CREDIT_TRANSFER("sepa-credit-transfers");

    private String paymentProductText;

    private static Map<PaymentType, SparebankPaymentProduct>
            tinkPaymentTypeToSparebankPaymentProductMapper = new HashMap<>();

    static {
        tinkPaymentTypeToSparebankPaymentProductMapper.put(
                PaymentType.DOMESTIC, NORWEGIAN_DOMESTIC_CREDIT_TRANSFER);
        tinkPaymentTypeToSparebankPaymentProductMapper.put(
                PaymentType.INTERNATIONAL, CROSS_BORDER_CREDIT_TRANSFER);
        tinkPaymentTypeToSparebankPaymentProductMapper.put(PaymentType.SEPA, SEPA_CREDIT_TRANSFER);
    }

    SparebankPaymentProduct(String text) {
        this.paymentProductText = text;
    }

    public String getText() {
        return paymentProductText;
    }

    public static SparebankPaymentProduct fromString(String text) {
        return Arrays.stream(SparebankPaymentProduct.values())
                .filter(s -> s.paymentProductText.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Can not map " + text + " to Sparebank payment product"));
    }

    public static SparebankPaymentProduct mapTinkPaymentTypeToSparebankPaymentProduct(
            PaymentType paymentType) {
        return Optional.ofNullable(tinkPaymentTypeToSparebankPaymentProductMapper.get(paymentType))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Cannot map Tink payment type: "
                                                + paymentType.toString()
                                                + " to Sparebank payment product"));
    }
}
