package se.tink.backend.aggregation.storage.logs;

import com.google.inject.Inject;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AgentHttpLogsCache {

    private final AgentHttpLogsMasker httpLogsMasker;
    private final RawHttpTrafficLogger rawHttpTrafficLogger;
    private final JsonHttpTrafficLogger jsonHttpTrafficLogger;

    private OptionalLogContent<String> aapLogContent;
    private OptionalLogContent<String> jsonLogContent;

    public Optional<String> getAapLogContent() {
        if (aapLogContent == null) {
            String log =
                    Optional.ofNullable(rawHttpTrafficLogger)
                            .flatMap(RawHttpTrafficLogger::tryGetLogContent)
                            .map(httpLogsMasker::maskSensitiveOutputLog)
                            .orElse(null);
            aapLogContent = OptionalLogContent.of(log);
        }
        return aapLogContent.getContent();
    }

    public Optional<String> getJsonLogContent() {
        if (jsonLogContent == null) {
            String log =
                    Optional.ofNullable(jsonHttpTrafficLogger)
                            .flatMap(JsonHttpTrafficLogger::tryGetLogContent)
                            .map(httpLogsMasker::maskSensitiveOutputLog)
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
