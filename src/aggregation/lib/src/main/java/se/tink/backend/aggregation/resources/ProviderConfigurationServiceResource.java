package se.tink.backend.aggregation.resources;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.client.provider_configuration.ProviderConfigurationService;
import se.tink.backend.aggregation.client.provider_configuration.rpc.ProviderConfiguration;
import se.tink.backend.aggregation.configuration.models.ProviderConfigurationServiceConfiguration;
import se.tink.libraries.http.client.WebResourceFactory;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class ProviderConfigurationServiceResource implements ProviderConfigurationService {

    private static final Logger logger =
            LoggerFactory.getLogger(ProviderConfigurationServiceResource.class);

    private final Client client;
    private final ProviderConfigurationServiceConfiguration configuration;

    @Inject
    public ProviderConfigurationServiceResource(
            ProviderConfigurationServiceConfiguration providerConfigurationServiceConfiguration) {
        Preconditions.checkNotNull(
                providerConfigurationServiceConfiguration,
                "ProviderConfigurationServiceConfiguration cannot be null.");

        this.configuration = providerConfigurationServiceConfiguration;

        logger.debug(
                String.format(
                        "Constructing ProviderConfigurationServiceClient towards url: %s.",
                        configuration.getHost() + ":" + configuration.getPort()));

        this.client = JerseyUtils.getClient(Collections.emptyList());

        logger.debug(
                String.format(
                        "Constructed ProviderConfigurationServiceClient towards url: %s.",
                        configuration.getHost() + ":" + configuration.getPort()));
    }

    private ProviderConfigurationService getProviderConfigurationService() {
        return WebResourceFactory.newResource(
                ProviderConfigurationService.class,
                client.resource(configuration.getHost() + ":" + configuration.getPort()));
    }

    @Override
    public List<ProviderConfiguration> list() {
        List<ProviderConfiguration> providerConfigurations =
                getProviderConfigurationService().list();
        return providerConfigurations;
    }

    @Override
    public List<ProviderConfiguration> listByMarket(String market) {
        return getProviderConfigurationService().listByMarket(market);
    }

    @Override
    public ProviderConfiguration getProviderByName(String providerName) {
        return getProviderConfigurationService().getProviderByName(providerName);
    }

    @Override
    public void addClientFilter(ClientFilter filter) {
        client.addFilter(filter);
    }

    @Override
    public void removeClientFilter(ClientFilter filter) {
        client.removeFilter(filter);
    }
}
