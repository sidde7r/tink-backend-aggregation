package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import java.time.ZoneId;

public class HandelsbankenConstants {

    public static final int MAX_FETCH_PERIOD_MONTHS = 13;

    public static class Time {
        public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");
    }

    public static class AccountMapper {
        public static final String CURRENT = "current";
        public static final String DEPOSIT = "deposit";
    }
}
