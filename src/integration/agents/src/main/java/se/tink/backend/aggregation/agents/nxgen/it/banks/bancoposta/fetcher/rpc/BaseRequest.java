package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseRequest {
    protected final Map<String, String> header =
            ImmutableMap.<String, String>builder()
                    .put("clientId", UUID.randomUUID().toString())
                    .put("requestId", "")
                    .build();
}
