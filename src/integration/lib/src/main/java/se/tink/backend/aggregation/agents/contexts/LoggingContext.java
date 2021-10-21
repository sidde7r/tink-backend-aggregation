package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.HttpJsonLogger;

public interface LoggingContext {

    /** @return HttpAapLogger or null if not configured */
    HttpAapLogger getHttpAapLogger();

    void setHttpAapLogger(HttpAapLogger httpAapLogger);

    /** @return HttpAapLogger or null if not configured */
    HttpJsonLogger getHttpJsonLogger();

    void setHttpJsonLogger(HttpJsonLogger httpJsonLogger);
}
