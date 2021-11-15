package se.tink.backend.aggregation.agents.tools.validator.builder;

import com.google.common.base.Preconditions;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.framework.provider.ProviderConfigurationUtil;
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
            ProviderConfig marketProviders =
                    ProviderConfigurationUtil.readProvidersConfiguration(market);
            this.provider = marketProviders.getProvider(providerName);
            this.provider.setMarket(marketProviders.getMarket());
            this.provider.setCurrency(marketProviders.getCurrency());
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
