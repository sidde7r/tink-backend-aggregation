package se.tink.agent.sdk.utils;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public interface Utilities {
    RandomGenerator getRandomGenerator();

    TimeGenerator getTimeGenerator();

    Sleeper getSleeper();

    TinkHttpClient getHttpClient();
}
