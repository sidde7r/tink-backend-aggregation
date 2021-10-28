package se.tink.backend.aggregation.storage.logs;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;

@Slf4j
public class AgentHttpLogsCache {

    private final AgentHttpLogsMasker httpLogsMasker;
    private final RawHttpTrafficLogger rawHttpTrafficLogger;
    private final JsonHttpTrafficLogger jsonHttpTrafficLogger;

    private final LogContentCache rawLogContent = new LogContentCache();
    private final LogContentCache jsonLogContent = new LogContentCache();

    @Inject
    public AgentHttpLogsCache(
            AgentHttpLogsMasker httpLogsMasker,
            RawHttpTrafficLogger rawHttpTrafficLogger,
            JsonHttpTrafficLogger jsonHttpTrafficLogger) {
        this.httpLogsMasker = httpLogsMasker;
        this.rawHttpTrafficLogger = rawHttpTrafficLogger;
        this.jsonHttpTrafficLogger = jsonHttpTrafficLogger;
    }

    public Optional<String> getRawLogContent() {
        if (rawLogContent.isNotInitialized()) {
            rawLogContent.initialize(getMaskedRawLogContent());
        }
        return rawLogContent.getContent();
    }

    private String getMaskedRawLogContent() {
        return Optional.ofNullable(rawHttpTrafficLogger)
                .flatMap(RawHttpTrafficLogger::tryGetLogContent)
                .map(httpLogsMasker::maskSensitiveOutputLog)
                .orElse(null);
    }

    public Optional<String> getJsonLogContent() {
        if (jsonLogContent.isNotInitialized()) {
            jsonLogContent.initialize(getMaskedJsonLogContent());
        }
        return jsonLogContent.getContent();
    }

    private String getMaskedJsonLogContent() {
        return Optional.ofNullable(jsonHttpTrafficLogger)
                .flatMap(JsonHttpTrafficLogger::tryGetLogContent)
                .map(httpLogsMasker::maskSensitiveOutputLog)
                .orElse(null);
    }

    private static class LogContentCache {

        private boolean initialized;
        private String content;

        boolean isNotInitialized() {
            return !initialized;
        }

        void initialize(String content) {
            this.content = content;
            this.initialized = true;
        }

        Optional<String> getContent() {
            return Optional.ofNullable(content);
        }
    }
}
