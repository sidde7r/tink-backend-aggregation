package se.tink.backend.aggregation.provider.configuration.resources;

import com.google.inject.Inject;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.provider.configuration.controllers.ProviderServiceController;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.backend.core.ProviderConfiguration;

public class ProviderServiceResource implements ProviderService {
    @Context
    private HttpServletRequest httpRequest;

    private final ProviderServiceController providerController;

    @Inject
    public ProviderServiceResource(ProviderServiceController providerController) {
        this.providerController = providerController;
    }

    private ClusterId getClusterId() {
        ClusterId clusterId = ClusterId.createFromHttpServletRequest(httpRequest);
        if (!clusterId.isValidId()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return clusterId;
    }

    @Override
    public List<ProviderConfiguration> list(String lang) {
        return providerController.list(Locale.forLanguageTag(lang), getClusterId());
    }

    @Override
    public List<ProviderConfiguration> listByMarket(String lang, String market) {
        return providerController.listByMarket(Locale.forLanguageTag(lang), getClusterId(), market);
    }

    @Override
    public ProviderConfiguration getProviderByName(String lang, String providerName) {
        return providerController.getProviderByName(Locale.forLanguageTag(lang), getClusterId(), providerName)
                .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
    }
}
