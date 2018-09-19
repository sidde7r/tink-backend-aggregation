package se.tink.backend.aggregation.resources;

import com.google.inject.Inject;
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

    @Inject
    public ProviderServiceResource(ProviderServiceController providerController) {
        this.providerController = providerController;
    }


    @Override
    public List<ProviderConfiguration> list(String lang, ClusterInfo clusterInfo) {
        return providerController.list(Locale.forLanguageTag(lang), clusterInfo.getClusterId());
    }

    @Override
    public List<ProviderConfiguration> listByMarket(String lang, String market, ClusterInfo clusterInfo) {
        return providerController.listByMarket(Locale.forLanguageTag(lang), clusterInfo.getClusterId(), market);
    }

    @Override
    public ProviderConfiguration getProviderByName(String lang, String providerName, ClusterInfo clusterInfo) {
        return providerController.getProviderByName(Locale.forLanguageTag(lang), clusterInfo.getClusterId(), providerName)
                .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
    }
}
