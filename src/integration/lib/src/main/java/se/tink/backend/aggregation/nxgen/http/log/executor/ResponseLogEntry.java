package se.tink.backend.aggregation.nxgen.http.log.executor;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseLogEntry {

    private final int status;
    private final Map<String, String> headers;
    private final String body;
}
