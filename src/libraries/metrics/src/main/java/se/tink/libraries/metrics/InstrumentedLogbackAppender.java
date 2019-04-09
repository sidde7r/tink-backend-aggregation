package se.tink.libraries.metrics;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

public class InstrumentedLogbackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private final Counter counter;

    InstrumentedLogbackAppender(CollectorRegistry registry) {
        counter =
                Counter.build()
                        .name("logback_appender_total")
                        .help("Logback log statements at various log levels")
                        .labelNames("level", "exception", "at")
                        .register(registry);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        IThrowableProxy throwable = event.getThrowableProxy();
        String exception = "";
        String at = "";
        if (throwable != null) {
            exception = throwable.getClassName();
            /* Try to find the first class the operator is likely to care about (i.e. Tink classes) */
            for (StackTraceElementProxy i : throwable.getStackTraceElementProxyArray()) {
                if (i.getStackTraceElement().getClassName().startsWith("se.tink")) {
                    at = i.getStackTraceElement().getClassName();
                    break;
                }
            }
        }

        switch (event.getLevel().toInt()) {
            case Level.TRACE_INT:
                counter.labels("trace", exception, at).inc();
                break;
            case Level.DEBUG_INT:
                counter.labels("debug", exception, at).inc();
                break;
            case Level.INFO_INT:
                counter.labels("info", exception, at).inc();
                break;
            case Level.WARN_INT:
                counter.labels("warn", exception, at).inc();
                break;
            case Level.ERROR_INT:
                counter.labels("error", exception, at).inc();
                break;
            default:
                break;
        }
    }
}
