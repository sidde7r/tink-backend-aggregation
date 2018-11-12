package se.tink.backend.aggregation.storage.database.daos;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;
import se.tink.backend.aggregation.storage.database.repositories.ClusterCryptoConfigurationRepository;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.CryptoId;

public class CryptoConfigurationDao {
    private ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository;

    @Inject
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

    public CryptoWrapper getCryptoWrapper(String clusterId) {
        List<ClusterCryptoConfiguration> clusterCryptoConfigurations = clusterCryptoConfigurationRepository
                .findByCryptoIdClusterId(clusterId);

        List<CryptoConfiguration> cryptoConfigurations = clusterCryptoConfigurations.stream()
                .map(CryptoConfigurationDao::convert)
                .collect(Collectors.toList());

        return CryptoWrapper.of(ImmutableList.copyOf(cryptoConfigurations));
    }
}
