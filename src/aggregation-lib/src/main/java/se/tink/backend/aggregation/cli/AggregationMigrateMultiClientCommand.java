package se.tink.backend.aggregation.cli;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configurations.models.AggregatorConfiguration;
import se.tink.backend.aggregation.configurations.models.ClientConfiguration;
import se.tink.backend.aggregation.configurations.models.ClusterConfiguration;
import se.tink.backend.aggregation.configurations.models.CryptoConfiguration;
import se.tink.backend.aggregation.configurations.models.CryptoConfigurationId;
import se.tink.backend.aggregation.configurations.repositories.AggregatorConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.ClientConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.ClusterConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.CryptoConfigurationsRepository;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.aggregation.clustercryptoconfiguration.ClusterCryptoConfigurationRepository;
import se.tink.backend.common.repository.mysql.aggregation.clusterhostconfiguration.ClusterHostConfigurationRepository;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.ClusterHostConfiguration;

public class AggregationMigrateMultiClientCommand extends AggregationServiceContextCommand<ServiceConfiguration> {


    private static final Logger log = LoggerFactory.getLogger(AggregationMigrateMultiClientCommand.class);

    public AggregationMigrateMultiClientCommand() {
        super("migrate-multi-client", "Only useful for migratation to the multi-client environment");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector) throws Exception {


        ClusterHostConfigurationRepository clusterHostConfigurationRepository =
                injector.getInstance(ClusterHostConfigurationRepository.class);
        ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository =
                injector.getInstance(ClusterCryptoConfigurationRepository.class);

        AggregatorConfigurationsRepository aggregatorConfigurationsRepository = injector.getInstance(AggregatorConfigurationsRepository.class);
        ClusterConfigurationsRepository clusterConfigurationsRepository = injector.getInstance(ClusterConfigurationsRepository.class);
        ClientConfigurationsRepository clientConfigurationsRepository = injector.getInstance(ClientConfigurationsRepository.class);
        CryptoConfigurationsRepository cryptoConfigurationsRepository = injector.getInstance(CryptoConfigurationsRepository.class);

        log.info("Starting migrating objects");

        List<ClusterHostConfiguration> clusterHostConfigurations = clusterHostConfigurationRepository.findAll();
        for (ClusterHostConfiguration clusterHostConfiguration : clusterHostConfigurations) {
            List<ClusterCryptoConfiguration> clusterCryptoConfigurations = clusterCryptoConfigurationRepository
                    .findByCryptoIdClusterId(clusterHostConfiguration.getClusterId());

            log.info("Completed creating ClusterConfiguration for id " + clusterHostConfiguration.getClusterId());
            ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
            clusterConfiguration.setClusterId(clusterHostConfiguration.getClusterId());
            clusterConfiguration.setBase64encodedclientcert(clusterHostConfiguration.getBase64EncodedClientCertificate());
            clusterConfiguration.setApiToken(clusterHostConfiguration.getApiToken());
            clusterConfiguration.setDisablerequestcompression(clusterHostConfiguration.isDisableRequestCompression());
            clusterConfiguration.setHost(clusterHostConfiguration.getHost());


            AggregatorConfiguration aggregatorConfiguration = new AggregatorConfiguration();
            aggregatorConfiguration.setAggregatorId(UUID.randomUUID().toString());
            aggregatorConfiguration.setAggregatorInfo(clusterHostConfiguration.getAggregatorIdentifier());
            
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setAggregatorId(aggregatorConfiguration.getAggregatorId());
            clientConfiguration.setApiClientKey(UUID.randomUUID().toString());
            clientConfiguration.setClientName(clusterHostConfiguration.getClusterId());
            clientConfiguration.setClusterId(clusterHostConfiguration.getClusterId());

            List<CryptoConfiguration> cryptoConfigurations = clusterCryptoConfigurations.stream().map(x -> {
                CryptoConfiguration cryptoConfiguration = new CryptoConfiguration();
                cryptoConfiguration.setBase64encodedkey(x.getBase64EncodedKey());
                cryptoConfiguration.setCryptoConfigurationId(
                        CryptoConfigurationId.of(x.getCryptoId().getKeyId(), x.getCryptoId().getClusterId()));
                return cryptoConfiguration;
            }).collect(Collectors.toList());

            clusterConfigurationsRepository.save(clusterConfiguration);
            aggregatorConfigurationsRepository.save(aggregatorConfiguration);
            clientConfigurationsRepository.save(clientConfiguration);
            cryptoConfigurationsRepository.save(cryptoConfigurations);
            log.info("Completed migration of cluster_host with id: " + clusterConfiguration.getClusterId());
        }

        log.info("Finished without errors");

    }

}
