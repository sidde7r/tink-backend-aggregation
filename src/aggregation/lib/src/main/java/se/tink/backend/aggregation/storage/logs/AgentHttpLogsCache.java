package se.tink.backend.aggregation.storage.logs;

import com.google.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;
import se.tink.libraries.se.tink.libraries.har_logger.src.logger.HarLogCollector;

@Slf4j
public class AgentHttpLogsCache {

    private final AgentHttpLogsMasker httpLogsMasker;
    private final RawHttpTrafficLogger rawHttpTrafficLogger;
    private final JsonHttpTrafficLogger jsonHttpTrafficLogger;
    private final HarLogCollector harLogCollector;

    private final LogContentCache rawLogContent = new LogContentCache();
    private final LogContentCache jsonLogContent = new LogContentCache();
    private final LogContentCache harLogContent = new LogContentCache();

    @Inject
    public AgentHttpLogsCache(
            AgentHttpLogsMasker httpLogsMasker,
            RawHttpTrafficLogger rawHttpTrafficLogger,
            JsonHttpTrafficLogger jsonHttpTrafficLogger,
            HarLogCollector harLogCollector) {
        this.httpLogsMasker = httpLogsMasker;
        this.rawHttpTrafficLogger = rawHttpTrafficLogger;
        this.jsonHttpTrafficLogger = jsonHttpTrafficLogger;
        this.harLogCollector = harLogCollector;
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

    public Optional<String> getHarLogContent() {
        if (harLogContent.isNotInitialized()) {
            harLogContent.initialize(getMaskedHarLogContent());
        }
        return harLogContent.getContent();
    }

    private String getMaskedHarLogContent() {
        if (harLogCollector == null || harLogCollector.isEmpty()) {
            return null;
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            harLogCollector.writeHar(outputStream, httpLogsMasker);
            return outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            log.error("Could not serialize har: ", e);
            return null;
        }
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
