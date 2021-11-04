package se.tink.backend.aggregation.resources;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.client.provider_configuration.ProviderConfigurationService;
import se.tink.backend.aggregation.client.provider_configuration.rpc.ProviderConfiguration;
import se.tink.backend.aggregation.configuration.models.ProviderConfigurationServiceConfiguration;
import se.tink.libraries.http.client.WebResourceFactory;
import se.tink.libraries.jersey.utils.ClientLoggingFilter;
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

        this.client = JerseyUtils.getClient(configuration.getPinnedCertificates());
        client.addFilter(new ClientLoggingFilter());

        logger.debug(
                String.format(
                        "Constructed ProviderConfigurationServiceClient towards url: %s.",
                        configuration.getHost() + ":" + configuration.getPort()));
        logger.info("[ProviderConfigurationServiceResource] Successfully initialised");
    }

    private ProviderConfigurationService getProviderConfigurationService() {
        return WebResourceFactory.newResource(
                ProviderConfigurationService.class,
                client.resource(configuration.getHost() + ":" + configuration.getPort()));
    }

    @Override
    public List<ProviderConfiguration> list(String clusterName, String clusterEnvironment) {
        logger.info("[ProviderConfigurationServiceResource] list method called");
        List<ProviderConfiguration> providerConfigurations =
                getProviderConfigurationService().list(clusterName, clusterEnvironment);
        logger.info("[ProviderConfigurationServiceResource] list method succeeded");
        return providerConfigurations;
    }

    @Override
    public List<ProviderConfiguration> listByMarket(
            String clusterName, String clusterEnvironment, String market) {
        logger.info("[ProviderConfigurationServiceResource] listByMarket method called");
        List<ProviderConfiguration> result =
                getProviderConfigurationService()
                        .listByMarket(clusterName, clusterEnvironment, market);
        logger.info("[ProviderConfigurationServiceResource] listByMarket method succeeded");
        return result;
    }

    @Override
    public List<ProviderConfiguration> listAll() {
        logger.info("[ProviderConfigurationServiceResource] listAll method called");
        List<ProviderConfiguration> result = getProviderConfigurationService().listAll();
        logger.info("[ProviderConfigurationServiceResource] listAll method succeeded");
        return result;
    }

    @Override
    public ProviderConfiguration getProviderByName(
            String clusterName, String clusterEnvironment, String providerName) {
        logger.info("[ProviderConfigurationServiceResource] getProviderByName method called");
        ProviderConfiguration result =
                getProviderConfigurationService()
                        .getProviderByName(clusterName, clusterEnvironment, providerName);
        logger.info("[ProviderConfigurationServiceResource] getProviderByName method succeeded");
        return result;
    }

    @Override
    public ProviderConfiguration getProviderByNameInClusterIfPossible(
            String clusterName, String clusterEnvironment, String providerName) {
        logger.info(
                "[ProviderConfigurationServiceResource] getProviderByNameInClusterIfPossible method called");
        ProviderConfiguration result =
                getProviderConfigurationService()
                        .getProviderByNameInClusterIfPossible(
                                clusterName, clusterEnvironment, providerName);
        logger.info(
                "[ProviderConfigurationServiceResource] getProviderByNameInClusterIfPossible method succeeded");
        return result;
    }
}
