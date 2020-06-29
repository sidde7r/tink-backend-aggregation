package se.tink.backend.aggregation.agents.modules.providers;

import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import java.net.URI;
import java.util.function.Function;
import se.tink.libraries.net.client.TinkApacheHttpClient4;

public interface LegacyAgentStrategyInterface {
    Function<String, URI> getLegacyHostStrategy();

    Function<ApacheHttpClient4Config, TinkApacheHttpClient4> getLegacyHttpClientStrategy();
}
