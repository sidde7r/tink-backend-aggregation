package se.tink.backend.aggregation.agents.agentfactory.utils;

import io.dropwizard.jackson.Jackson;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.ProviderConfig;

@NoArgsConstructor
public class ProviderReader {

    public Set<Provider> getProviderConfigurations(String folderForConfigurations) {
        return Arrays.asList(new File(folderForConfigurations).listFiles()).stream()
                .filter(file -> file.getName().contains("providers-"))
                .filter(file -> !file.getName().contains("development"))
                .map(this::readProviderConfiguration)
                .flatMap(this::setMarketAndCurrency)
                .collect(Collectors.toSet());
    }

    private ProviderConfig readProviderConfiguration(File file) {
        try {
            return Jackson.newObjectMapper().readValue(file, ProviderConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<Provider> setMarketAndCurrency(ProviderConfig config) {
        return config.getProviders().stream()
                .peek(
                        provider -> {
                            provider.setMarket(config.getMarket());
                            provider.setCurrency(config.getCurrency());
                        });
    }
}
