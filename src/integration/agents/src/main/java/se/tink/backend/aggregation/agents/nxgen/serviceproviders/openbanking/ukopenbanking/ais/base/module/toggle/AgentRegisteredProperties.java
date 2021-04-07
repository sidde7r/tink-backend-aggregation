package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import java.util.EnumMap;
import java.util.Map;

class AgentRegisteredProperties {
    private static final String DEFAULT_VALUE = "";
    private final Map<Constants, String> properties;

    AgentRegisteredProperties(Map<String, String> rawProperties) {
        this.properties = new EnumMap<>(Constants.class);
        this.properties.put(
                Constants.PROVIDER_NAME, rawProperties.get(Constants.PROVIDER_NAME.getValue()));
        this.properties.put(Constants.APP_ID, rawProperties.get(Constants.APP_ID.getValue()));
    }

    String getCurrentProviderName() {
        return properties.getOrDefault(Constants.PROVIDER_NAME, DEFAULT_VALUE);
    }

    String getCurrentAppId() {
        return properties.getOrDefault(Constants.APP_ID, DEFAULT_VALUE);
    }

    enum Constants {
        PROVIDER_NAME("providerName"),
        APP_ID("appId");

        private final String value;

        Constants(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }
}
