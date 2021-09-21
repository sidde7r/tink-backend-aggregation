package se.tink.backend.aggregation.storage.debug;

import com.google.inject.Inject;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.HttpJsonLogger;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AgentDebugLogsCache {

    private final AgentDebugLogsMasker debugLogsMasker;
    private final HttpAapLogger httpAapLogger;
    private final HttpJsonLogger httpJsonLogger;

    private OptionalLogContent<String> aapLogContent;
    private OptionalLogContent<String> jsonLogContent;

    public Optional<String> getAapLogContent() {
        if (aapLogContent == null) {
            String log =
                    Optional.ofNullable(httpAapLogger)
                            .flatMap(HttpAapLogger::tryGetLogContent)
                            .map(debugLogsMasker::maskSensitiveOutputLog)
                            .orElse(null);
            aapLogContent = OptionalLogContent.of(log);
        }
        return aapLogContent.getContent();
    }

    public Optional<String> getJsonLogContent() {
        if (jsonLogContent == null) {
            String log =
                    Optional.ofNullable(httpJsonLogger)
                            .flatMap(HttpJsonLogger::tryGetLogContent)
                            .map(debugLogsMasker::maskSensitiveOutputLog)
                            .orElse(null);
            jsonLogContent = OptionalLogContent.of(log);
        }
        return jsonLogContent.getContent();
    }

    @RequiredArgsConstructor
    private static class OptionalLogContent<T> {

        private final T content;

        Optional<T> getContent() {
            return Optional.ofNullable(content);
        }

        static <T> OptionalLogContent<T> of(@Nullable T content) {
            return new OptionalLogContent<>(content);
        }
    }
}
