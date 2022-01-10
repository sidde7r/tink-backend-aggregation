package src.agent_sdk.linter.test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTestCases {
    public void preventDate() {
        // BUG: Diagnostic contains: Disallowed usage of method or class.
        new Date();
    }

    public void preventLocalDate() {
        // BUG: Diagnostic contains: Disallowed usage of method or class.
        LocalDate.now();

        // BUG: Diagnostic contains: Disallowed usage of method or class.
        LocalDate.now(ZoneId.of("UTC"));

        // BUG: Diagnostic contains: Disallowed usage of method or class.
        LocalDate.now(Clock.systemDefaultZone());
    }

    public void preventLocalDateTime() {
        // BUG: Diagnostic contains: Disallowed usage of method or class.
        LocalDateTime.now();

        // BUG: Diagnostic contains: Disallowed usage of method or class.
        LocalDateTime.now(ZoneId.of("UTC"));

        // BUG: Diagnostic contains: Disallowed usage of method or class.
        LocalDateTime.now(Clock.systemDefaultZone());
    }

    public void preventInstant() {
        // BUG: Diagnostic contains: Disallowed usage of method or class.
        Instant.now();

        // BUG: Diagnostic contains: Disallowed usage of method or class.
        Instant.now(Clock.systemDefaultZone());
    }
}
