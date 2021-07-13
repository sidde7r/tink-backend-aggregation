package se.tink.backend.aggregation.configuration.models;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import se.tink.backend.aggregation.workers.commands.state.configuration.AapFileProvider;

public class AggregationDecoupledAapFileProvider implements AapFileProvider {

    private final Map<String, Set<String>> providerToAapFile;

    @Inject
    private AggregationDecoupledAapFileProvider() {
        // TODO: Read the map from a YML file
        providerToAapFile = new HashMap<>();
        providerToAapFile.put(
                "uk-americanexpress-password",
                ImmutableSet.of("data/agents/uk/amex/refresh-traffic.aap"));
        providerToAapFile.put(
                "uk-barclays-oauth2",
                ImmutableSet.of(
                        "data/agents/uk/barclays/mock_log.aap",
                        "data/agents/uk/barclays/payment_mock_log.aap"));
        providerToAapFile.put(
                "it-unicredit-oauth2",
                ImmutableSet.of("data/agents/it/unicredit/payment_mock_log.aap"));
    }

    @Override
    public Set<String> getAapFilePaths(String providerName) {
        if (!providerToAapFile.containsKey(providerName)) {
            throw new IllegalStateException(
                    "There is no AAP file specified for provider " + providerName);
        }
        return providerToAapFile.get(providerName);
    }
}
