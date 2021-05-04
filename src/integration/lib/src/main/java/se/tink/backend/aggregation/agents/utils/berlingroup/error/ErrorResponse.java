package se.tink.backend.aggregation.agents.utils.berlingroup.error;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@JsonObject
public class ErrorResponse {

    @Getter private String psuMessage;
    private List<TppMessage> tppMessages;

    public List<TppMessage> getTppMessages() {
        return tppMessages == null ? Collections.emptyList() : tppMessages;
    }

    public static Optional<ErrorResponse> fromHttpException(
            HttpResponseException httpResponseException) {
        if (!httpResponseException.getResponse().hasBody()) {
            return Optional.empty();
        }
        return Optional.ofNullable(getBodyAsExpectedType(httpResponseException.getResponse()));
    }

    private static ErrorResponse getBodyAsExpectedType(HttpResponse response) {
        try {
            return response.getBody(ErrorResponse.class);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static Predicate<ErrorResponse> psuMessageContainsPredicate(String matcher) {
        return errorResponse ->
                Optional.ofNullable(errorResponse)
                        .map(ErrorResponse::getPsuMessage)
                        .map(String::toLowerCase)
                        .filter(psuMessage -> psuMessage.contains(matcher.toLowerCase()))
                        .isPresent();
    }

    public static Predicate<ErrorResponse> anyTppMessageMatchesPredicate(TppMessage matcher) {
        return errorResponse ->
                errorResponse != null
                        && errorResponse.getTppMessages().stream()
                                .anyMatch(x -> x.matches(matcher));
    }
}
