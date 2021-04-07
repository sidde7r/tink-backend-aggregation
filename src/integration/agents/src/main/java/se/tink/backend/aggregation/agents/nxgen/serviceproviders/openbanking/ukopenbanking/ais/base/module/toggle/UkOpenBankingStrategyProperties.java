package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class UkOpenBankingStrategyProperties {
    private static final String DEFAULT_VALUE = "";
    private static final String COMMA = ",";
    private final Map<Constants, String> properties;

    UkOpenBankingStrategyProperties(Map<String, String> rawProperties) {
        this.properties = new EnumMap<>(Constants.class);
        this.properties.put(
                Constants.PROVIDER_NAME, rawProperties.get(Constants.PROVIDER_NAME.getValue()));
        this.properties.put(
                Constants.EXCLUDED_APP_IDS,
                rawProperties.get(Constants.EXCLUDED_APP_IDS.getValue()));
    }

    public List<String> getProvidersNames() {
        return Stream.of(
                        properties
                                .getOrDefault(Constants.PROVIDER_NAME, DEFAULT_VALUE)
                                .split(COMMA))
                .collect(Collectors.toList());
    }

    public List<String> getExcludedAppIds() {
        return Stream.of(
                        properties
                                .getOrDefault(Constants.EXCLUDED_APP_IDS, DEFAULT_VALUE)
                                .split(COMMA))
                .collect(Collectors.toList());
    }

    enum Constants {
        PROVIDER_NAME("providersNames"),
        EXCLUDED_APP_IDS("excludedAppIds");

        private final String value;

        Constants(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }
}
