package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import java.util.Random;

public class AutostartTokenGenerator {

    public static String generateFrom(Random random) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 101; // letter 'e'
        int targetStringLength = 32;
        // Autostart token example: 8dce0218-9cc2-0263-3904-aec17576ec3a
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()
                .replaceAll("(.{8})(.{4})(.{4})(.{4})", "$1-$2-$3-$4-");
    }
}
