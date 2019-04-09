package se.tink.backend.aggregation.controllers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;
import se.tink.backend.aggregation.storage.database.repositories.AggregatorConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClientConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClusterConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;

public class ProvisionClientController {
    private static final Logger log = LoggerFactory.getLogger(ProvisionClientController.class);

    private AggregatorConfigurationsRepository aggregatorConfigurationsRepository;
    private ClusterConfigurationsRepository clusterConfigurationsRepository;
    private ClientConfigurationsRepository clientConfigurationsRepository;
    private CryptoConfigurationsRepository cryptoConfigurationsRepository;
    private static String availableCluster = "oxford-production";
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    public ProvisionClientController(
            AggregatorConfigurationsRepository aggregatorConfigurationsRepository,
            ClusterConfigurationsRepository clusterConfigurationsRepository,
            ClientConfigurationsRepository clientConfigurationsRepository,
            CryptoConfigurationsRepository cryptoConfigurationsRepository) {
        this.aggregatorConfigurationsRepository = aggregatorConfigurationsRepository;
        this.clusterConfigurationsRepository = clusterConfigurationsRepository;
        this.clientConfigurationsRepository = clientConfigurationsRepository;
        this.cryptoConfigurationsRepository = cryptoConfigurationsRepository;
    }

    public void provision(String clientName, String aggregatorIdentifier) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(clientName), "Client cannot be null.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(availableCluster), "Cluster name cannot be null.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aggregatorIdentifier),
                "Aggregator Id or identifier value should not be null.");

        ClusterConfiguration clusterConfiguration = clusterConfigurationsRepository.findOne(availableCluster);
        Preconditions.checkNotNull(clusterConfiguration, "Cluster configuration could not be found.");

        ClientConfiguration existingClientConfiguration = clientConfigurationsRepository.findOne(clientName);
        if (!Objects.isNull(existingClientConfiguration)) {
            log.info(String.format("We found another entry for that %s in the database.", existingClientConfiguration.getClientName()));
            return;
        }

        AggregatorConfiguration aggregatorConfiguration = createAggregatorIdentifier(aggregatorIdentifier);

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setApiClientKey(UUID.randomUUID().toString());
        clientConfiguration.setAggregatorId(aggregatorConfiguration.getAggregatorId());
        clientConfiguration.setClientName(clientName);
        clientConfiguration.setClusterId(clusterConfiguration.getClusterId());

        CryptoConfiguration cryptoConfiguration = new CryptoConfiguration();
        cryptoConfiguration.setBase64encodedkey(getBase64EncodedKey());
        cryptoConfiguration.setCryptoConfigurationId(CryptoConfigurationId.of(1, clientName));

        // TODO: make these transactional
        // fail all together or succeed all together.
        aggregatorConfigurationsRepository.save(aggregatorConfiguration);
        clientConfigurationsRepository.save(clientConfiguration);
        cryptoConfigurationsRepository.save(cryptoConfiguration);

        log.info("--------------------------");
        log.info("Client {} has been added.", clientName);
        log.info("Api key: {}", clientConfiguration.getApiClientKey());
        log.info("Identifiable as: {}", aggregatorConfiguration.getAggregatorInfo());
        log.info("Please store the encrypted keys on an offline drive.");
    }

    private AggregatorConfiguration createAggregatorIdentifier(
            String aggregatorIdentifier) {
        AggregatorConfiguration aggregatorConfiguration = new AggregatorConfiguration();
        aggregatorConfiguration.setAggregatorId(UUID.randomUUID().toString());
        aggregatorConfiguration.setAggregatorInfo(aggregatorIdentifier);
        return aggregatorConfiguration;
    }

    private String getBase64EncodedKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return BASE64_ENCODER.encodeToString(bytes);
    }
}
