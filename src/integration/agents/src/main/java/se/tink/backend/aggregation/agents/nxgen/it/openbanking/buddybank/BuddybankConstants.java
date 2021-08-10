package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public class BuddybankConstants {

    private BuddybankConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String PICKUP_CODE_FAILURE = "Unexpected pickup code message format";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StorageKeys {
        public static final String CONSENT_ID = "CONSENT_ID";
        public static final String STATE = "STATE";
    }
}
