package se.tink.backend.aggregation.nxgen.http.log.executor;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestLogEntry {

    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;
}
