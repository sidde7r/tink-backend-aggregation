package se.tink.backend.aggregation.resources;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.client.provider_configuration.ProviderConfigurationService;
import se.tink.backend.aggregation.client.provider_configuration.rpc.ProviderConfiguration;
import se.tink.backend.aggregation.configuration.models.ProviderConfigurationServiceConfiguration;

public class FakeProviderConfigurationService implements ProviderConfigurationService {

    private static final Logger logger =
            LoggerFactory.getLogger(FakeProviderConfigurationService.class);

    @Inject
    public FakeProviderConfigurationService(
            ProviderConfigurationServiceConfiguration configuration) {
        Preconditions.checkNotNull(
                configuration, "ProviderConfigurationServiceConfiguration cannot be null.");

        logger.debug(
                String.format(
                        "Constructing fake provider configuration service client towards url: %s.",
                        configuration.getHost() + ":" + configuration.getPort()));
    }

    @Override
    public List<ProviderConfiguration> list(String clusterName, String clusterEnvironment) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<ProviderConfiguration> listByMarket(
            String clusterName, String clusterEnvironment, String market) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<ProviderConfiguration> listAll() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ProviderConfiguration getProviderByName(
            String clusterName, String clusterEnvironment, String providerName) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ProviderConfiguration getProviderByNameInClusterIfPossible(
            String clusterName, String clusterEnvironment, String providerName) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
