package se.tink.backend.aggregation.resources;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.api.ProviderService;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.controllers.ProviderServiceController;
import se.tink.backend.core.ProviderConfiguration;

public class ProviderServiceResource implements ProviderService {
    @Context
    private HttpServletRequest httpRequest;

    private final ProviderServiceController providerController;
    private final boolean isAggregationCluster;

    @Inject
    public ProviderServiceResource(ProviderServiceController providerController,
            @Named("isAggregationCluster") boolean isAggregationCluster) {
        this.providerController = providerController;
        this.isAggregationCluster = isAggregationCluster;
    }

    private ClusterId getClusterId() {

        ClusterId clusterId = ClusterId.createFromRequest(httpRequest);
        if (!clusterId.isValidId()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return clusterId;
    }

    @Override
    public List<ProviderConfiguration> list(String lang) {
        if (isAggregationCluster) {
            return providerController.list(Locale.forLanguageTag(lang), getClusterId());
        }
        return providerController.list(Locale.forLanguageTag(lang));
    }

    @Override
    public List<ProviderConfiguration> listByMarket(String lang, String market) {
        if (isAggregationCluster) {
            return providerController.listByMarket(Locale.forLanguageTag(lang), getClusterId(), market);
        }
        return providerController.listByMarket(Locale.forLanguageTag(lang), market);
    }

    @Override
    public ProviderConfiguration getProviderByName(String lang, String providerName) {
        if (isAggregationCluster) {
            return providerController.getProviderByName(Locale.forLanguageTag(lang), getClusterId(), providerName)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        }

        return providerController.getProviderByName(Locale.forLanguageTag(lang), providerName)
                .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
    }
}
