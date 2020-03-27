package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class PayloadParser {
    @AllArgsConstructor
    @Getter
    public static class Payload {
        private String blz;
        private String endpoint;
        private String bankName;
    }

    public static Payload parse(String payload) {
        String[] splitPayload = payload.split(" ");
        return new Payload(splitPayload[0], splitPayload[1], splitPayload[2]);
    }
}
