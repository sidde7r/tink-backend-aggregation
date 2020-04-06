package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration;

import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class PayloadParser {
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ");

    private PayloadParser() {}

    @AllArgsConstructor
    @Getter
    public static class Payload {
        private String blz;
        private String endpoint;
        private String bankName;
    }

    public static Payload parse(String payload) {
        String[] splitPayload = SPACE_PATTERN.split(payload);
        return new Payload(splitPayload[0], splitPayload[1], splitPayload[2]);
    }
}
