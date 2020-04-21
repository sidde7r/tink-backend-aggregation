package se.tink.backend.aggregation.configuration.models;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.workers.commands.state.configuration.AapFileProvider;

public class AggregationDecoupledAapFileProvider implements AapFileProvider {

    private final Map<String, String> providerToAapFile;

    @Inject
    private AggregationDecoupledAapFileProvider() {
        // TODO: Read the map from a YML file
        providerToAapFile = new HashMap<>();
        providerToAapFile.put(
                "uk-americanexpress-password",
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62/resources/amex-refresh-traffic.aap");
        providerToAapFile.put(
                "uk-barclays-oauth2",
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/barclays/mock/resources/barclays_mock_log.aap");
    }

    @Override
    public String getAapFilePath(String providerName) {
        if (!providerToAapFile.keySet().contains(providerName)) {
            throw new IllegalStateException(
                    "There is no AAP file specified for provider " + providerName);
        }
        return providerToAapFile.get(providerName);
    }
}
