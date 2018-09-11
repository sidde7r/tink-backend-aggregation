package se.tink.backend.aggregation.provider.configuration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.cli.provider.ProviderConfigModel;
import se.tink.backend.core.ProviderConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProviderFileModule extends AbstractModule {

    protected static ObjectMapper mapper = new ObjectMapper();
    public static final Logger log = LoggerFactory.getLogger(ProviderRepositoryModule.class);

    @Override
    protected void configure() { }

    @Provides
    @Singleton
    @Named("providerConfiguration")
    public Map<String, ProviderConfiguration> providerConfigurationByProviderName() throws IOException {
        return loadProviderConfigurationFromJson();
    }

    protected Map<String, ProviderConfiguration> loadProviderConfigurationFromJson() throws IOException {
        log.info("Seeding providers");
        File directory = new File("data/seeding");
        File[] providerFiles = directory.listFiles((dir, fileName) -> fileName.matches("providers-[a-z]{2}.json"));

        Map<String, ProviderConfiguration> providerConfigurationByProviderName = Maps.newHashMap();

        if (providerFiles == null) {
            throw new IOException("no provider file found");
        }

        for (File providerFile : providerFiles) {
            log.info("Seeding from file " + providerFile.getName());
            seedProvider(providerFile, providerConfigurationByProviderName);
        }

        return providerConfigurationByProviderName;
    }

    private void seedProvider(File providerFile, Map<String, ProviderConfiguration> providerConfigurationByProviderName)
            throws IOException {
        ProviderConfigModel providerConfig = mapper.readValue(providerFile, ProviderConfigModel.class);

        String currency = providerConfig.getCurrency();
        String market = providerConfig.getMarket();
        List<ProviderConfiguration> providers = providerConfig.getProviders();

        for (ProviderConfiguration providerConfiguration : providers) {
            if (market != null) {
                providerConfiguration.setMarket(market);
            } else {
                market = "DEVELOPMENT";
            }

            if (currency != null) {
                providerConfiguration.setCurrency(currency);
            }

            providerConfigurationByProviderName.put(providerConfiguration.getName(), providerConfiguration);
        }
        log.info("Seeded " + providers.size() + " providers for " + market);
    }
}
