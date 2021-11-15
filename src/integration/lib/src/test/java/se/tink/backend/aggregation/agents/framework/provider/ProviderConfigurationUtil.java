package se.tink.backend.aggregation.agents.framework.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import se.tink.backend.agents.rpc.Provider;
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
            ProviderConfig providerConfig = mapper.readValue(providersFile, ProviderConfig.class);
            return applyProviderOverrides(providerConfig);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String escapeMarket(String market) {
        return market.replaceAll("[^a-zA-Z]", "");
    }

    private static ProviderConfig applyProviderOverrides(ProviderConfig providerConfig) {
        // Overrides file is an array of complete providers
        File overridesFile = new File("etc/provider-override-development.json");
        if (!overridesFile.exists()) {
            return providerConfig;
        }

        final String market = providerConfig.getMarket();
        final ObjectMapper mapper = new ObjectMapper();
        try {
            List<Provider> providers =
                    mapper.readValue(overridesFile, new TypeReference<List<Provider>>() {});
            providers.stream()
                    .filter(provider -> market.equals(provider.getMarket()))
                    .forEach(provider -> addProviderOverride(providerConfig, provider));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return providerConfig;
    }

    private static void addProviderOverride(ProviderConfig providerConfig, Provider newProvider) {
        providerConfig
                .getProviders()
                .removeIf(oldProvider -> oldProvider.getName().equals(newProvider.getName()));
        providerConfig.getProviders().add(newProvider);
    }
}
