package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class TimeStampProvider {
    public static String getTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }
}
