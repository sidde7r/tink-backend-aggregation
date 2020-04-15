package se.tink.backend.aggregation.agents.tools.validator.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.tools.validator.ClientConfigurationValidator;
import se.tink.backend.aggregation.configuration.ProviderConfig;

public class ClientConfigurationValidatorBuilderForTest {
    private final Provider provider;

    private ClientConfigurationValidatorBuilderForTest(Builder builder) {
        this.provider = builder.getProvider();
    }

    public ClientConfigurationValidator getClientConfigurationValidator() {
        return new ClientConfigurationValidator(provider);
    }

    public Provider getProvider() {
        return provider;
    }

    public static class Builder {
        private final Provider provider;

        public Builder(String market, String providerName) {
            ProviderConfig marketProviders = readProvidersConfiguration(market);
            this.provider = marketProviders.getProvider(providerName);
            this.provider.setMarket(marketProviders.getMarket());
            this.provider.setCurrency(marketProviders.getCurrency());
        }

        private static ProviderConfig readProvidersConfiguration(String market) {
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

        public Provider getProvider() {
            return provider;
        }

        public ClientConfigurationValidatorBuilderForTest build() {
            Preconditions.checkNotNull(provider, "Provider was not set.");
            return new ClientConfigurationValidatorBuilderForTest(this);
        }
    }
}
