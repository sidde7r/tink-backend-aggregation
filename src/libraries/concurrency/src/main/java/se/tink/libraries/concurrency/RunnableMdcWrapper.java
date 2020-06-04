package se.tink.libraries.concurrency;

import java.util.Map;
import org.slf4j.MDC;

public class RunnableMdcWrapper {
    // Set the MDC context on the new thread
    public static Runnable wrap(Runnable runnable) {
        final Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            final Map<String, String> previousContext = MDC.getCopyOfContextMap();
            try {
                if (context != null) {
                    MDC.setContextMap(context);
                } else {
                    MDC.clear();
                }
                runnable.run();
            } finally {
                if (previousContext != null) {
                    MDC.setContextMap(previousContext);
                } else {
                    MDC.clear();
                }
            }
        };
    }
}
