package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NordeaFiConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class RetryFilter {
        public static final int NUM_TIMEOUT_RETRIES = 5;
        public static final int RETRY_SLEEP_MILLISECONDS = 3000;
    }
}
