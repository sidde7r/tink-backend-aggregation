package se.tink.backend.aggregation.agents.standalone.mapper.providers;

import java.util.Map;

public interface CommonExternalParametersProvider {
    /**
     * This method should return all required parameters for common request based on configuration
     *
     * @return required common parameters map
     */
    Map<String, String> buildExternalParametersMap();
}
