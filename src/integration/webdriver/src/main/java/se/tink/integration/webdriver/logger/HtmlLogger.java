package se.tink.integration.webdriver.logger;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLogger;

@RequiredArgsConstructor
public class HtmlLogger {

    private static final String LOG_TEMPLATE =
            "[%s] - [WEB DRIVER] %s\n\n REQUEST URL:\n%s\n\nRESPONSE HTML:\n\n %s";

    private final WebDriver webDriver;
    private final HttpAapLogger httpAapLogger;

    public void info(String message) {
        log("INFO", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    private void log(String level, String message) {
        String logMessage =
                String.format(
                        LOG_TEMPLATE,
                        level,
                        message,
                        webDriver.getCurrentUrl(),
                        webDriver.getPageSource());
        httpAapLogger.logRawUnsafe(logMessage + "\n");
    }
}
