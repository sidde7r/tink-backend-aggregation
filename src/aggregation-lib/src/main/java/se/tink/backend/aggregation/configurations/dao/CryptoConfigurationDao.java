package se.tink.backend.aggregation.configurations.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.configurations.models.CryptoConfiguration;
import se.tink.backend.aggregation.configurations.models.CryptoConfigurationId;
import se.tink.backend.aggregation.configurations.repositories.ClusterCryptoConfigurationRepository;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.CryptoId;

public class CryptoConfigurationDao {
    private ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository;

    public CryptoConfigurationDao(ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository) {
        this.clusterCryptoConfigurationRepository = clusterCryptoConfigurationRepository;
    }

    public static CryptoConfiguration convert(ClusterCryptoConfiguration clusterCrypto) {
        CryptoConfiguration configuration = new CryptoConfiguration();
        configuration.setBase64encodedkey(clusterCrypto.getBase64EncodedKey());
        configuration.setCryptoConfigurationId(
                CryptoConfigurationId.of(
                        clusterCrypto.getCryptoId().getKeyId(),
                        clusterCrypto.getCryptoId().getClusterId()));
        return configuration;
    }

    public Optional<CryptoConfiguration> getClusterCryptoConfigurationFromClusterId(String clusterId) {
        // Get the most recent (keyId, key) for clusterId.getId()
        List<ClusterCryptoConfiguration> clusterCryptoConfigurations = clusterCryptoConfigurationRepository
                .findByCryptoIdClusterId(clusterId);

        // Get the highest (most recent) keyId.
        Optional<ClusterCryptoConfiguration> optionalClusterCryptoConfiguration = clusterCryptoConfigurations.stream().max(
                Comparator.comparing(t -> t.getCryptoId().getKeyId()));
        return optionalClusterCryptoConfiguration.map(CryptoConfigurationDao::convert);
    }

    public Optional<byte[]> getClusterKeyFromKeyId(String clusterId, int keyId) {
        CryptoId cryptoId = new CryptoId();
        cryptoId.setClusterId(clusterId);
        cryptoId.setKeyId(keyId);
        ClusterCryptoConfiguration clusterCryptoConfiguration = clusterCryptoConfigurationRepository
                .findByCryptoId(cryptoId);

        if (Objects.isNull(clusterCryptoConfiguration)) {
            return Optional.empty();
        }

        return Optional.of(clusterCryptoConfiguration.getDecodedKey());
    }
}
