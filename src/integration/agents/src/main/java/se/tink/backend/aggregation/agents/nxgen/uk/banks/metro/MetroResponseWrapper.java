package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

import static io.vavr.API.Match;

import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import io.vavr.API.Match.Case;
import io.vavr.control.Either;
import io.vavr.control.Option;
import java.util.Objects;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.libraries.serialization.utils.SerializationUtils;

public interface MetroResponseWrapper<T> {

    static <T> MetroResponseWrapper<T> of(ResponseEntity<String> response, Class<T> mapTo) {
        Objects.requireNonNull(response);
        Objects.requireNonNull(mapTo);
        return new Success<>(response, mapTo);
    }

    static <T> MetroResponseWrapper<T> failure(
            ResponseEntity<String> response, AgentBankApiError apiError) {
        Objects.requireNonNull(response);
        Objects.requireNonNull(apiError);
        return new Failure<>(response, apiError);
    }

    @SuppressWarnings({"unchecked", "varargs"})
    default MetroResponseWrapper<T> mapFailure(
            Case<ResponseEntity<String>, AgentBankApiError>... cases) {
        Option<AgentBankApiError> option = Match(getRequest()).option(cases);
        return option.isEmpty()
                ? of(getRequest(), classResponse())
                : failure(getRequest(), option.get());
    }

    ResponseEntity<String> getRequest();

    Class<T> classResponse();

    boolean isSuccess();

    AgentBankApiError getAgentBankApiError();

    default Either<AgentBankApiError, T> wrap() {
        if (this.isSuccess()) {
            return Either.right(
                    SerializationUtils.deserializeFromString(
                            getRequest().getBody(), classResponse()));
        }
        return Either.left(getAgentBankApiError());
    }

    class Success<T> implements MetroResponseWrapper<T> {

        private final ResponseEntity<String> function;
        private final Class<T> mapTo;

        public Success(ResponseEntity<String> function, Class<T> mapTo) {
            this.function = function;
            this.mapTo = mapTo;
        }

        @Override
        public ResponseEntity<String> getRequest() {
            return function;
        }

        @Override
        public Class<T> classResponse() {
            return mapTo;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public AgentBankApiError getAgentBankApiError() {
            throw new UnsupportedOperationException("Getting Agent error is unsupported");
        }
    }

    class Failure<T> implements MetroResponseWrapper<T> {

        private final ResponseEntity<String> function;
        private final AgentBankApiError apiError;

        public Failure(ResponseEntity<String> function, AgentBankApiError apiError) {
            this.function = function;
            this.apiError = apiError;
        }

        @Override
        public ResponseEntity<String> getRequest() {
            return function;
        }

        @Override
        public Class<T> classResponse() {
            throw new UnsupportedOperationException("`classResponse` is unsupported");
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public AgentBankApiError getAgentBankApiError() {
            return apiError;
        }
    }
}
