package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

public final class NemIdCodeAppConstants {
    public static class Status {
        public static final String STATUS_OK = "ok";
        public static final String STATUS_TIMEOUT = "timeout";
        public static final String OVERWRITTEN = "overwritten";
        public static final String EXPIRED = "expired";
    }

    public static class Errors {
        public static final String READ_TIMEOUT_ERROR = "Read timed out";
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
