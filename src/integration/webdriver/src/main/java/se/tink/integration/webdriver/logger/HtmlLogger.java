package se.tink.integration.webdriver.logger;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;
import se.tink.libraries.har_logger.src.model.HarEntry;
import se.tink.libraries.har_logger.src.model.HarRequest;
import se.tink.libraries.har_logger.src.model.HarResponse;

@RequiredArgsConstructor
public class HtmlLogger {

    private static final String LOG_TEMPLATE =
            "[%s] - [WEB DRIVER] %s\n\n REQUEST URL:\n%s\n\nRESPONSE HTML:\n\n %s";
    private static final String HAR_COMMENT_TEMPLATE = "[WEB DRIVER] %s: %s";

    private final WebDriver webDriver;
    private final RawHttpTrafficLogger rawHttpTrafficLogger;
    private final Consumer<HarEntry> harEntryConsumer;

    public void info(String message) {
        log("INFO", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    private void log(String level, String message) {
        String currentUrl = webDriver.getCurrentUrl();
        String pageSource = webDriver.getPageSource();
        String logMessage = String.format(LOG_TEMPLATE, level, message, currentUrl, pageSource);
        if (rawHttpTrafficLogger != null) {
            rawHttpTrafficLogger.logRawUnsafe(logMessage + "\n");
        }

        if (harEntryConsumer != null) {
            harEntryConsumer.accept(
                    buildHarEntry(
                            currentUrl,
                            pageSource,
                            String.format(HAR_COMMENT_TEMPLATE, level, message)));
        }
    }

    /* Build a HarEntry from a URL and page source, making up some fields */
    private HarEntry buildHarEntry(String url, String pageSource, String comment) {
        return new HarEntry(
                HarRequest.builder()
                        .timestamp(new Date())
                        .method("GET")
                        .url(url)
                        .httpVersion("HTTP/1.1")
                        .headers(Collections.emptyMap())
                        .body(null)
                        .build(),
                HarResponse.builder()
                        .statusCode(203)
                        .statusText("Non-Authoritative Information")
                        .httpVersion("HTTP/1.1")
                        .headers(Collections.emptyMap())
                        .body(pageSource.getBytes(StandardCharsets.UTF_8))
                        .comment(comment)
                        .build(),
                1);
    }
}
