package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums;

import java.util.Arrays;

public enum EnterCardCurrency {
    NOK,
    SEK,
    DKK;

    public String toString() {
        return name();
    }

    public static EnterCardCurrency fromString(String text) {
        return Arrays.stream(EnterCardCurrency.values())
                .filter(s -> s.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unrecognized EnterCard currency : " + text));
    }
}
