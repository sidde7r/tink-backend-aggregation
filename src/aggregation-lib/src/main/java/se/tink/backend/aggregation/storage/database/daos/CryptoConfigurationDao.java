package se.tink.backend.aggregation.storage.database.daos;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;
import se.tink.backend.aggregation.storage.database.repositories.ClusterCryptoConfigurationRepository;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
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

    public Optional<CryptoConfiguration> getClusterCryptoConfigurationFromClusterId(ClusterId clusterId) {
        // Get the most recent (keyId, key) for clusterId.getId()
        List<ClusterCryptoConfiguration> clusterCryptoConfigurations = clusterCryptoConfigurationRepository
                .findByCryptoIdClusterId(clusterId.getId());

        // Get the highest (most recent) keyId.
        Optional<ClusterCryptoConfiguration> optionalClusterCryptoConfiguration = clusterCryptoConfigurations.stream().max(
                Comparator.comparing(t -> t.getCryptoId().getKeyId()));
        return optionalClusterCryptoConfiguration.map(CryptoConfigurationDao::convert);
    }

    public Optional<byte[]> getClusterKeyFromKeyId(ClusterId clusterId, int keyId) {
        CryptoId cryptoId = new CryptoId();
        cryptoId.setClusterId(clusterId.getId());
        cryptoId.setKeyId(keyId);
        ClusterCryptoConfiguration clusterCryptoConfiguration = clusterCryptoConfigurationRepository
                .findByCryptoId(cryptoId);

        if (Objects.isNull(clusterCryptoConfiguration)) {
            return Optional.empty();
        }

        return Optional.of(clusterCryptoConfiguration.getDecodedKey());
    }

    public CryptoWrapper getCryptoWrapper(String clusterId) {
        List<ClusterCryptoConfiguration> clusterCryptoConfigurations = clusterCryptoConfigurationRepository
                .findByCryptoIdClusterId(clusterId);

        List<CryptoConfiguration> cryptoConfigurations = clusterCryptoConfigurations.stream()
                .map(CryptoConfigurationDao::convert)
                .collect(Collectors.toList());

        return CryptoWrapper.of(ImmutableList.copyOf(cryptoConfigurations));
    }
}
