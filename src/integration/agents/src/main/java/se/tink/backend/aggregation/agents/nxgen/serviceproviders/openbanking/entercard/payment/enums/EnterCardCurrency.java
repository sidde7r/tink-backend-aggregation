package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.payment.enums;

import java.util.Arrays;

public enum EnterCardCurrency {
    NOK,
    SEK,
    DKK;

    public static EnterCardCurrency fromString(String text) {
        return Arrays.stream(EnterCardCurrency.values())
                .filter(s -> s.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unrecognized EnterCard currency : " + text));
    }

    public String toString() {
        return name();
    }
}
