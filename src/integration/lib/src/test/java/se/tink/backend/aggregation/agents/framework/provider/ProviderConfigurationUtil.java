package se.tink.backend.aggregation.agents.framework.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import se.tink.backend.aggregation.configuration.ProviderConfig;

public final class ProviderConfigurationUtil {
    private ProviderConfigurationUtil() {}

    public static ProviderConfig readProvidersConfiguration(String market) {
        String providersFilePath =
                "external/tink_backend/src/provider_configuration/data/seeding/providers-"
                        + escapeMarket(market).toLowerCase()
                        + ".json";
        File providersFile = new File(providersFilePath);
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(providersFile, ProviderConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String escapeMarket(String market) {
        return market.replaceAll("[^a-zA-Z]", "");
    }
}
