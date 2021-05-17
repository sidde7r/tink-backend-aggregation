package se.tink.backend.aggregation.agents.legacy.banks.seb;

import com.sun.jersey.api.client.WebResource.Builder;
import java.net.URI;
import java.util.UUID;
import java.util.function.Function;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.libraries.net.client.TinkApacheHttpClient4;

public class SebBaseApiClient {
    private final TinkApacheHttpClient4 client;
    private final Function<String, URI> uriFunction;
    private final String sebUUID;
    private static final String BASE_URL = "https://mp.seb.se";

    public SebBaseApiClient(TinkApacheHttpClient4 client, Function<String, URI> uriFunction) {
        this.client = client;
        this.uriFunction = uriFunction;
        this.sebUUID = UUID.randomUUID().toString().toUpperCase();
    }

    public Builder resource(String url) {
        return client.resource(uriFunction.apply(BASE_URL + url))
                .header("User-Agent", CommonHeaders.DEFAULT_USER_AGENT)
                .header(SEBApiConstants.HeaderKeys.X_SEB_UUID, sebUUID)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }
}
