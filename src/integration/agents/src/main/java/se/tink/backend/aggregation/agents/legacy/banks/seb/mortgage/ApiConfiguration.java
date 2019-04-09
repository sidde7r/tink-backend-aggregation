package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import java.util.Map;

public interface ApiConfiguration {
    String getBaseUrl();

    boolean isHttps();

    Map<String, String> getHeaders();
}
