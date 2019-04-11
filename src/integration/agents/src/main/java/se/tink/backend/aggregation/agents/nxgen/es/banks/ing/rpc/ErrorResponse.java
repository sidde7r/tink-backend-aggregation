package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.rpc;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private List<MessageEntity> message;

    public boolean hasError() {
        return Objects.nonNull(message)
                && message.stream().anyMatch(m -> !Strings.isNullOrEmpty(m.getErrorCode()));
    }

    public boolean hasErrorCode(String errorCode) {
        return Objects.nonNull(message)
                && message.stream().anyMatch(m -> errorCode.equalsIgnoreCase(m.getErrorCode()));
    }

    public Optional<String> getErrorSummary() {
        if (!hasError()) {
            return Optional.empty();
        }

        return Optional.of(
                message.stream()
                        .map(
                                m ->
                                        String.format(
                                                "%s:%s:%s",
                                                m.getField(), m.getErrorCode(), m.getMessage()))
                        .collect(Collectors.joining(",")));
    }
}
