package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.seb.SEBApiAgent;
import se.tink.backend.common.config.SEBMortgageIntegrationConfiguration;

public class SEBMortgageApiConfiguration implements ApiConfiguration {
    private static final String NO_CONFIGURATION_ERROR =
            String.format(
                    "setConfiguration(…) hasn't been called before instantiating. Did you forget to do this in %s?",
                    SEBApiAgent.class.getSimpleName());

    private boolean https;
    private String baseUrl;
    private String apiVersion;
    private HashMap<String, String> headers;
    private boolean hasBeenConfigured;

    @Override
    public String getBaseUrl() {
        Preconditions.checkState(hasBeenConfigured, NO_CONFIGURATION_ERROR);
        return baseUrl;
    }

    @Override
    public boolean isHttps() {
        Preconditions.checkState(hasBeenConfigured, NO_CONFIGURATION_ERROR);
        return https;
    }

    @Override
    public Map<String, String> getHeaders() {
        Preconditions.checkState(hasBeenConfigured, NO_CONFIGURATION_ERROR);
        return headers;
    }

    public void setConfiguration(SEBMortgageIntegrationConfiguration configuration) {
        this.hasBeenConfigured = true;
        this.baseUrl = String.format(
                "%s/tink/loans/%s/mortgages",
                configuration.getTargetHost(),
                configuration.getApiVersion());
        this.https = configuration.isHttps();
        this.headers = configuration.getHttpHeaders();
    }
}
