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
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
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


    @Override
    public List<ProviderConfiguration> list(String lang, ClusterInfo clusterInfo) {
        if (isAggregationCluster) {
            return providerController.list(Locale.forLanguageTag(lang), clusterInfo.getClusterId());
        }
        return providerController.list(Locale.forLanguageTag(lang));
    }

    @Override
    public List<ProviderConfiguration> listByMarket(String lang, String market, ClusterInfo clusterInfo) {
        if (isAggregationCluster) {
            return providerController.listByMarket(Locale.forLanguageTag(lang), clusterInfo.getClusterId(), market);
        }
        return providerController.listByMarket(Locale.forLanguageTag(lang), market);
    }

    @Override
    public ProviderConfiguration getProviderByName(String lang, String providerName, ClusterInfo clusterInfo) {
        if (isAggregationCluster) {
            return providerController.getProviderByName(Locale.forLanguageTag(lang), clusterInfo.getClusterId(), providerName)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        }

        return providerController.getProviderByName(Locale.forLanguageTag(lang), providerName)
                .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
    }
}
