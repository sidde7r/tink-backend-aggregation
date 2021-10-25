package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;

public interface LoggingContext {

    /** @return HttpAapLogger or null if not configured */
    RawHttpTrafficLogger getRawHttpTrafficLogger();

    void setRawHttpTrafficLogger(RawHttpTrafficLogger rawHttpTrafficLogger);

    /** @return HttpAapLogger or null if not configured */
    JsonHttpTrafficLogger getJsonHttpTrafficLogger();

    void setJsonHttpTrafficLogger(JsonHttpTrafficLogger jsonHttpTrafficLogger);
}
