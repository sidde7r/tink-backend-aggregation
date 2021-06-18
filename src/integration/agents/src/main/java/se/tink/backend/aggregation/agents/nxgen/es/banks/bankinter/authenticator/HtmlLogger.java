package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.openqa.selenium.WebDriver;

public class HtmlLogger {
    private static final String LOG_TEMPLATE = "[%s]: %s\n\nRESPONSE:\n\n %s";
    private final WebDriver webDriver;
    private final PrintStream printLogStream;

    public HtmlLogger(WebDriver webDriver, OutputStream logStream) {
        this.webDriver = webDriver;
        try {
            this.printLogStream = new PrintStream(logStream, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    private void log(String level, String message) {
        String logMessage = String.format(LOG_TEMPLATE, level, message, webDriver.getPageSource());
        printLogStream.println(logMessage);
    }
}
