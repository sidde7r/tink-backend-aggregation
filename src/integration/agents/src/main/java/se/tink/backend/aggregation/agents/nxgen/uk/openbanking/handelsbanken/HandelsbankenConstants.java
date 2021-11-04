package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import java.time.ZoneOffset;

public class HandelsbankenConstants {

    public static final int MAX_FETCH_PERIOD_MONTHS = 13;
    public static final String PROVIDER_NAME = "handelsbanken";
    public static final String CERT_ID = "DEFAULT";

    public static class ExceptionMessages {
        public static final String AVAILABLE_BALANCE_NOT_FOUND = "Available balance not found.";
    }

    public static class Time {
        public static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.UTC;
    }

    public static class AccountMapper {
        public static final String CURRENT = "current";
        public static final String DEPOSIT = "deposit";
    }
}
