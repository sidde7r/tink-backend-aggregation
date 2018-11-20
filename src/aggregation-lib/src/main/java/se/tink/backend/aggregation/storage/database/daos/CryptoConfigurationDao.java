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
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.CryptoId;

public class CryptoConfigurationDao {
    private ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository;
    private CryptoConfigurationsRepository cryptoConfigurationsRepository;

    @Inject
    CryptoConfigurationDao(ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository,
            CryptoConfigurationsRepository cryptoConfigurationsRepository) {
        this.clusterCryptoConfigurationRepository = clusterCryptoConfigurationRepository;
        this.cryptoConfigurationsRepository = cryptoConfigurationsRepository;
    }

    public CryptoWrapper getCryptoWrapperOfClientName(String clientName) {
        return CryptoWrapper.of(
                ImmutableList.copyOf(
                        cryptoConfigurationsRepository.findByCryptoConfigurationIdClientName(clientName)));
    }
}
