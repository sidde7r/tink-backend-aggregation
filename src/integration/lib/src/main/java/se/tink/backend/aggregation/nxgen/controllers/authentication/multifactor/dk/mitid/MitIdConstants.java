package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

@UtilityClass
public class MitIdConstants {

    public static final LogTag MIT_ID_LOG_TAG = LogTag.from("[MitID]");

    @UtilityClass
    public static class WaitTime {
        public static final int WAIT_FOR_FIRST_AUTHENTICATION_SCREEN = 20;
        public static final int WAIT_TO_DETECT_2FA_SCREEN = 10;

        public static final int WAIT_FOR_CODE_APP_SCREEN = 10;
        public static final int WAIT_FOR_CODE_APP_POLLING_RESULTS = 300;

        public static final int WAIT_TO_CHECK_IF_THERE_IS_CHANGE_METHOD_LINK = 1;
        public static final int WAIT_FOR_METHOD_SELECTOR_SCREEN = 5;
        public static final int WAIT_FOR_ANY_SELECT_METHOD_BUTTON = 2;

        public static final int WAIT_TO_CHECK_IF_USER_HAS_TO_ENTER_CPR = 10;
        public static final int WAIT_TO_GIVE_CPR_SCREEN_TIME_TO_EXIT = 3;
        public static final int WAIT_TO_CHECK_IF_AUTH_FINISHED = 10;

        public static final int WAIT_TO_CHECK_IF_FOUND_SCREEN_WAS_NOT_REPLACED_WITH_ERROR_SCREEN =
                2;
    }

    @UtilityClass
    public static class ProxyListenerKeys {
        public static final String LISTEN_CODE_APP_POLLING_FINISHED = "LISTEN_POLL_CODE_APP";
        public static final String LISTEN_WEB_AUTH_FINISHED = "LISTEN_WEB_AUTH_FINISHED";
    }

    @UtilityClass
    public static class Errors {
        public static final Multimap<MitIdError, String> ERROR_MESSAGE_MAPPING =
                ImmutableMultimap.<MitIdError, String>builder()
                        .putAll(
                                MitIdError.USER_ID_DOES_NOT_EXIST,
                                "User does not exist",
                                "Bruger-ID eksisterer ikke")
                        .putAll(
                                MitIdError.CODE_APP_TOO_MANY_REQUESTS,
                                "Your MitID app has received several requests at the same time",
                                "Din MitID app har modtaget flere samtidige anmodninger")
                        .putAll(
                                MitIdError.CODE_APP_TEMPORARILY_LOCKED,
                                "Din MitID app er midlertidigt spærret")
                        .putAll(
                                MitIdError.SESSION_TIMEOUT,
                                "Your session timed out",
                                "Din session er udløbet")
                        /*
                        We're not sure what is this error about. One way to reproduce it
                        is to try to log into a bank as user that does not have account in it.
                         */
                        .putAll(MitIdError.UNSPECIFIED_ERROR, "Uspecificeret fejl")
                        .build();

        public static Optional<MitIdError> findErrorForMessage(String message) {
            return ERROR_MESSAGE_MAPPING.entries().stream()
                    .filter(entry -> message.toLowerCase().contains(entry.getValue().toLowerCase()))
                    .map(Map.Entry::getKey)
                    .findFirst();
        }
    }
}
