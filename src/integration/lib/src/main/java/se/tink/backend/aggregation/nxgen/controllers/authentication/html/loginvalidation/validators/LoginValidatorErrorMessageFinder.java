package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;

public class LoginValidatorErrorMessageFinder implements LoginValidator<String> {

    private List<MessagesExceptionPair> messagesExceptionPairs = new LinkedList<>();

    public LoginValidatorErrorMessageFinder(
            ConnectivityException exceptionToThrow, String... messagesForFinding) {
        addMessagesExceptionPair(exceptionToThrow, messagesForFinding);
    }

    @Override
    public void validate(String body) {
        messagesExceptionPairs.stream()
                .filter(o -> containsAllMessages(body, o.messages))
                .findAny()
                .ifPresent(
                        o -> {
                            throw o.connectivityException;
                        });
    }

    private boolean containsAllMessages(String body, List<String> messages) {
        for (String messageToFind : messages) {
            if (!body.contains(messageToFind)) {
                return false;
            }
        }
        return true;
    }

    public LoginValidatorErrorMessageFinder addMessagesExceptionPair(
            ConnectivityException exceptionToThrow, String... messagesForFinding) {
        Preconditions.checkArgument(
                messagesForFinding.length > 0, "At least one message is required");
        messagesExceptionPairs.add(
                new MessagesExceptionPair(Arrays.asList(messagesForFinding), exceptionToThrow));
        return this;
    }

    @AllArgsConstructor
    @Getter
    private class MessagesExceptionPair {
        private List<String> messages;
        private ConnectivityException connectivityException;
    }
}
