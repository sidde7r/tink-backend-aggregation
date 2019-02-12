package se.tink.libraries.accesslogging;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessLoggingUtils {
    private static final Logger log = LoggerFactory.getLogger("ACCESS");

    public static void log(AccessLoggingRequestDetails command) {
        log.info(process(command));
    }

    private static String process(AccessLoggingRequestDetails command) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(command.getRemoteHost())
                .append(" ")
                .append(command.getRequestString())
                .append(" ")
                .append(command.getResponseStatus())
                .append(" - \"")
                .append(command.getUserAgent())
                .append("\" ")
                .append(command.getResponseTimeString())
                .append(" ")
                // Add user data.
                .append("[")
                .append(command.getUserId() != null
                        ? "userId:" + command.getUserId()
                        : "")
                .append(" ")
                .append(command.getOauthClientId() != null
                        ? "clientId:" + command.getOauthClientId()
                        : "")
                .append(" ");

        if (command.getHttpAuthenticationMethod() != null) {
            stringBuilder
                    .append("Authorization:")
                    .append(command.getHttpAuthenticationMethod());
            if (!Strings.isNullOrEmpty(command.getSessionId())) {
                stringBuilder
                        .append(" ")
                        // Only show first 6 characters of Authentication header.
                        .append(command.getSessionId().substring(0, 7));
            }
        }
        stringBuilder.append("]");

        if (command.getBody() != null) {
            stringBuilder
                    .append(" - ")
                    .append(command.getBody());
        }

        return stringBuilder.toString();
    }
}
