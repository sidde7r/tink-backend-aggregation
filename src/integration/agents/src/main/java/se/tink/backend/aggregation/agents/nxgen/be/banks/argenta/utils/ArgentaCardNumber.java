package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.utils;

import java.util.stream.IntStream;

public class ArgentaCardNumber {
    public static String formatCardNumber(String cc) {
        String creditCard = cc.replaceAll("\\s", "");
        StringBuilder result = new StringBuilder();
        IntStream.range(0, creditCard.length())
                .forEach(
                        i -> {
                            if (i != 0 && i % 4 == 0) result.append(" ");
                            result.append(creditCard.charAt(i));
                        });
        return result.toString();
    }
}
