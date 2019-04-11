package se.tink.backend.aggregation.provider.configuration.http.resources;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.backend.aggregation.provider.configuration.cluster.identifiers.ClusterInfo;
import se.tink.backend.aggregation.provider.configuration.controllers.ProviderServiceController;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationCore;
import se.tink.backend.aggregation.provider.configuration.http.converter.HttpProviderConfigurationConverter;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;

public class ProviderServiceResource implements ProviderService {
    private final ProviderServiceController providerController;

    @Inject
    public ProviderServiceResource(ProviderServiceController providerController) {
        this.providerController = providerController;
    }

    @Override
    public List<ProviderConfigurationDTO> list(ClusterInfo clusterInfo) {
        return HttpProviderConfigurationConverter.convert(
                providerController.list(clusterInfo.getClusterId()),
                clusterInfo.getClusterId().getId());
    }

    @Override
    public List<ProviderConfigurationDTO> listByMarket(String market, ClusterInfo clusterInfo) {
        return HttpProviderConfigurationConverter.convert(
                providerController.listByMarket(clusterInfo.getClusterId(), market),
                clusterInfo.getClusterId().getId());
    }

    @Override
    public ProviderConfigurationDTO getProviderByName(
            String providerName, ClusterInfo clusterInfo) {
        Optional<ProviderConfigurationCore> providerConfiguration =
                providerController.getProviderByName(clusterInfo.getClusterId(), providerName);

        if (!providerConfiguration.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return HttpProviderConfigurationConverter.convert(
                clusterInfo.getClusterId().getId(), providerConfiguration.get());
    }
}
