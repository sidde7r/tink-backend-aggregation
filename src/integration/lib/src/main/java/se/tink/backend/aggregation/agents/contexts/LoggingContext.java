package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLogger;

public interface LoggingContext {

    /** @return HttpAapLogger or null if not configured */
    HttpAapLogger getHttpAapLogger();

    void setHttpAapLogger(HttpAapLogger httpAapLogger);
}
