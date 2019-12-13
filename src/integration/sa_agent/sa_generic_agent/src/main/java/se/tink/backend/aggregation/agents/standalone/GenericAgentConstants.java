package se.tink.backend.aggregation.agents.standalone;

public final class GenericAgentConstants {

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class PersistentStorageKey {
        public static final String CONSENT_ID = "consentId";
    }

    public static final String NEXT_KEY = "nextKey";
}
