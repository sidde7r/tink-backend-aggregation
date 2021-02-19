package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NemIdCodeAppConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Status {
        public static final String STATUS_OK = "ok";
        public static final String STATUS_TIMEOUT = "timeout";
        public static final String OVERWRITTEN = "overwritten";
        public static final String EXPIRED = "expired";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Errors {
        public static final String READ_TIMEOUT_ERROR = "Read timed out";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public enum UserMessage implements LocalizableEnum {
        NEM_ID_PROCESS_INIT("NemId authentication process initialized"),
        VERIFYING_CREDS("Verifying credentials"),
        VALID_CREDS("Credentials are valid"),
        OPEN_NEM_ID_APP("Please open the NemId app and confirm login"),
        OPEN_NEM_ID_APP_AND_CLICK_BUTTON(
                "Please open the NemId app and confirm login. Then click the \"Submit\" button"),
        PROVIDE_CODE_CARD_CODE("Please provide NemId code card key"),
        PROVIDE_CODE_TOKEN_CODE("Please provide NemId code token code"),
        ENTER_ACTIVATION_PASSWORD("Enter activation password.");

        private final LocalizableKey message;

        UserMessage(String message) {
            this.message = new LocalizableKey(message);
        }

        @Override
        public LocalizableKey getKey() {
            return message;
        }
    }
}
