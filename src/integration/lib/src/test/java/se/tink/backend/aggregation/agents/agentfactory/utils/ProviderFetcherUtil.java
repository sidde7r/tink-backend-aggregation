package se.tink.backend.aggregation.agents.agentfactory.utils;

import io.dropwizard.jackson.Jackson;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.ProviderConfig;

public class ProviderFetcherUtil {

    private String folderForConfigurations;

    public ProviderFetcherUtil(String folderForConfigurations) {
        this.folderForConfigurations = folderForConfigurations;
    }

    // Must be a list because order is important since we want to have a deterministic
    // behavior when picking a provider per agent.
    public List<Provider> getProviderConfigurations() {
        List<Provider> result =
                Arrays.asList(new File(folderForConfigurations).listFiles()).stream()
                        .filter(file -> file.getName().contains("providers-"))
                        .filter(file -> !file.getName().contains("development"))
                        .map(this::readProviderConfiguration)
                        .flatMap(this::setMarketAndCurrency)
                        .collect(Collectors.toList());

        result.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        return result;
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
