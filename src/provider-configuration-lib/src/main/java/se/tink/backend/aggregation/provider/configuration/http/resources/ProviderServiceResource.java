package se.tink.backend.aggregation.provider.configuration.http.resources;

import com.google.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.provider.configuration.controllers.ProviderServiceController;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.http.converter.HttpProviderConfigurationConverter;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;
import se.tink.libraries.http.utils.HttpResponseHelper;

public class ProviderServiceResource implements ProviderService {
    @Context
    private HttpServletRequest httpRequest;

    private final ProviderServiceController providerController;

    @Inject
    public ProviderServiceResource(ProviderServiceController providerController) {
        this.providerController = providerController;
    }

    @Override
    public List<ProviderConfigurationDTO> list(String lang, ClusterInfo clusterInfo) {
        return HttpProviderConfigurationConverter.convert(providerController.list(Locale.forLanguageTag(lang), clusterInfo.getClusterId()));
    }

    @Override
    public List<ProviderConfigurationDTO> listByMarket(String lang, String market, ClusterInfo clusterInfo) {
        return HttpProviderConfigurationConverter.convert(providerController.listByMarket(Locale.forLanguageTag(lang), clusterInfo.getClusterId(), market));
    }

    @Override
    public ProviderConfigurationDTO getProviderByName(String lang, String providerName, ClusterInfo clusterInfo) {
        Optional<ProviderConfiguration> providerConfiguration = providerController.getProviderByName(Locale.forLanguageTag(lang), clusterInfo.getClusterId(), providerName);
        if (!providerConfiguration.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return HttpProviderConfigurationConverter.convert(providerConfiguration.get());
    }
}
