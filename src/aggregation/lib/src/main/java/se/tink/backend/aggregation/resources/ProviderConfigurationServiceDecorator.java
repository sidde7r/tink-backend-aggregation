package se.tink.backend.aggregation.resources;

import com.sun.jersey.api.client.filter.ClientFilter;
import java.util.List;
import se.tink.backend.aggregation.client.filter.ClusterIdFilter;
import se.tink.backend.aggregation.client.provider_configuration.ProviderConfigurationService;
import se.tink.backend.aggregation.client.provider_configuration.rpc.ProviderConfiguration;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class ProviderConfigurationServiceDecorator implements ProviderConfigurationService {

    private final ProviderConfigurationService providerConfigurationService;
    private final ClusterIdFilter clusterIdFilter;

    private ProviderConfigurationServiceDecorator(
            ProviderConfigurationService providerConfigurationService, ClientInfo clientInfo) {
        this.providerConfigurationService = providerConfigurationService;
        this.clusterIdFilter = new ClusterIdFilter(clientInfo.getClusterId());
    }

    public static ProviderConfigurationServiceDecorator of(
            ProviderConfigurationService providerConfigurationService, ClientInfo clientInfo) {
        return new ProviderConfigurationServiceDecorator(providerConfigurationService, clientInfo);
    }

    @Override
    public List<ProviderConfiguration> list() {
        providerConfigurationService.addClientFilter(clusterIdFilter);
        List<ProviderConfiguration> providerConfigurations = providerConfigurationService.list();
        providerConfigurationService.removeClientFilter(clusterIdFilter);
        return providerConfigurations;
    }

    @Override
    public List<ProviderConfiguration> listByMarket(String market) {
        providerConfigurationService.addClientFilter(clusterIdFilter);
        List<ProviderConfiguration> providerConfigurations =
                providerConfigurationService.listByMarket(market);
        providerConfigurationService.removeClientFilter(clusterIdFilter);
        return providerConfigurations;
    }

    @Override
    public ProviderConfiguration getProviderByName(String providerName) {
        providerConfigurationService.addClientFilter(clusterIdFilter);
        ProviderConfiguration providerConfiguration =
                providerConfigurationService.getProviderByName(providerName);
        providerConfigurationService.removeClientFilter(clusterIdFilter);
        return providerConfiguration;
    }

    @Override
    public void addClientFilter(ClientFilter filter) {
        throw new NotImplementedException(
                "ProviderConfigurationServiceDecorator does not implement addClientFilter");
    }

    @Override
    public void removeClientFilter(ClientFilter filter) {
        throw new NotImplementedException(
                "ProviderConfigurationServiceDecorator does not implement removeFilter");
    }
}
