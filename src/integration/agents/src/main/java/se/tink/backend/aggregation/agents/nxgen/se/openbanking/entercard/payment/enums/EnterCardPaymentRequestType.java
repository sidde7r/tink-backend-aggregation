package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums;

import java.util.Arrays;

public enum EnterCardPaymentRequestType {
    FT,
    BP,
    BT;

    public String toString() {
        return name();
    }

    public static EnterCardPaymentRequestType fromString(String text) {
        return Arrays.stream(EnterCardPaymentRequestType.values())
                .filter(s -> s.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unrecognized EnterCard account type : " + text));
    }
}
