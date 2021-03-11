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
        NEM_ID_PROCESS_INIT(new LocalizableKey("NemId authentication process initialized")),
        VERIFYING_CREDS(new LocalizableKey("Verifying credentials")),
        VALID_CREDS(new LocalizableKey("Credentials are valid")),
        OPEN_NEM_ID_APP(new LocalizableKey("Please open the NemId app and confirm login")),
        OPEN_NEM_ID_APP_AND_CLICK_BUTTON(
                new LocalizableKey(
                        "Please open the NemId app and confirm login. Then click the \"Submit\" button")),
        PROVIDE_CODE_CARD_CODE(new LocalizableKey("Please provide NemId code card key")),
        PROVIDE_CODE_TOKEN_CODE(new LocalizableKey("Please provide NemId code token code")),
        CHOOSE_NEM_ID_METHOD(new LocalizableKey("Please choose NemId authentication method")),
        ENTER_ACTIVATION_PASSWORD(new LocalizableKey("Enter activation password.")),
        ENTER_6_DIGIT_CODE(new LocalizableKey("Enter 6 digits code."));

        private final LocalizableKey message;

        UserMessage(LocalizableKey message) {
            this.message = message;
        }

        @Override
        public LocalizableKey getKey() {
            return message;
        }
    }
}
