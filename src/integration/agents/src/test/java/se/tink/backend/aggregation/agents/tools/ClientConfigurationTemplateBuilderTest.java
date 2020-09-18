package se.tink.backend.aggregation.agents.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.ProviderConfig;

@Ignore
public class ClientConfigurationTemplateBuilderTest {
    private final Provider provider;
    private final boolean includeDescriptions;
    private final boolean includeExamples;

    private ClientConfigurationTemplateBuilderTest(Builder builder) {
        this.provider = builder.getProvider();
        this.includeDescriptions = builder.getIncludeDescriptions();
        this.includeExamples = builder.getIncludeExamples();
    }

    public ClientConfigurationTemplateBuilder getClientConfigurationTemplateBuilder() {
        return new ClientConfigurationTemplateBuilder(
                provider, includeDescriptions, includeExamples);
    }

    public static class Builder {
        private final Provider provider;
        private final boolean includeDescriptions;
        private final boolean includeExamples;

        public Builder(
                String market,
                String providerName,
                boolean includeDescriptions,
                boolean includeExamples) {
            ProviderConfig marketProviders = readProvidersConfiguration(market);
            this.provider = marketProviders.getProvider(providerName);
            this.provider.setMarket(marketProviders.getMarket());
            this.provider.setCurrency(marketProviders.getCurrency());
            this.includeDescriptions = includeDescriptions;
            this.includeExamples = includeExamples;
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

        public boolean getIncludeDescriptions() {
            return includeDescriptions;
        }

        public boolean getIncludeExamples() {
            return includeExamples;
        }

        public ClientConfigurationTemplateBuilderTest build() {
            Preconditions.checkNotNull(provider, "Provider was not set.");
            return new ClientConfigurationTemplateBuilderTest(this);
        }
    }
}
