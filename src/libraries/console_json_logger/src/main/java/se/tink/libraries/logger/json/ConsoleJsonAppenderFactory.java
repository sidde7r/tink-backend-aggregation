package se.tink.libraries.logger.json;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.jackson.JacksonJsonFormatter;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.AbstractAppenderFactory;

/**
 * Custom Console appender JSON factory to support json logging, inspired by:
 * https://gist.github.com/ajmath/e9f90c29cd224653c218
 *
 * <p>In the configuration, appenders.type needs to comply with {@link #APPENDER_TYPE_NAME} (see
 * example below).
 *
 * <p>Note: It is equally important that the resource file:
 * src/libraries/console_json_logger/src/main/resources/META-INF/services/io.dropwizard.logging.AppenderFactory
 * has the fully qualified class name of this class.
 *
 * <p>Configuration example to use JSON logging.
 *
 * <pre>
 * logging:
 * level: INFO
 * loggers:
 * org.hibernate: WARN
 * ...
 * appenders:
 * - type: console-json
 * ...
 * </pre>
 */
@JsonTypeName(ConsoleJsonAppenderFactory.APPENDER_TYPE_NAME)
public class ConsoleJsonAppenderFactory extends AbstractAppenderFactory {

    static final String APPENDER_TYPE_NAME = "console-json";

    private String appenderName = "console-json-appender";
    private boolean includeContextName = true;

    @JsonProperty
    public String getName() {
        return this.appenderName;
    }

    @JsonProperty
    public void setName(String name) {
        this.appenderName = name;
    }

    @JsonProperty
    public boolean getIncludeContextName() {
        return this.includeContextName;
    }

    @JsonProperty
    public void setIncludeContextName(boolean includeContextName) {
        this.includeContextName = includeContextName;
    }

    @Override
    public Appender<ILoggingEvent> build(
            LoggerContext loggerContext, String s, Layout<ILoggingEvent> providedLayout) {
        JsonLayout layout = createJsonLayout();

        LayoutWrappingEncoder<ILoggingEvent> layoutEncoder = new LayoutWrappingEncoder<>();
        layoutEncoder.setLayout(layout);

        ConsoleAppender<ILoggingEvent> appender = createAppender(loggerContext, layoutEncoder);

        return wrapAsync(appender);
    }

    private JsonLayout createJsonLayout() {
        JsonLayout layout = new JsonLayout();
        layout.setJsonFormatter(new JacksonJsonFormatter());
        layout.setAppendLineSeparator(true);
        layout.setIncludeException(true);
        layout.setIncludeContextName(includeContextName);
        // Need to start the layout otherwise it will default to not include the full stacktrace
        // (hidden bug).
        layout.start();

        return layout;
    }

    private ConsoleAppender<ILoggingEvent> createAppender(
            LoggerContext loggerContext, LayoutWrappingEncoder<ILoggingEvent> layoutEncoder) {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setName(appenderName);
        appender.setContext(loggerContext);
        appender.setEncoder(layoutEncoder);
        addThresholdFilter(appender, threshold);
        appender.start();

        return appender;
    }
}
