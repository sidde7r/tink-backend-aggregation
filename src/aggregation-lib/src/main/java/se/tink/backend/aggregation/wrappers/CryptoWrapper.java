package se.tink.backend.aggregation.wrappers;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;

public class CryptoWrapper {
    private final ImmutableList<CryptoConfiguration> cryptoConfigurations;

    private CryptoWrapper(ImmutableList<CryptoConfiguration> cryptoConfigurations) {
        this.cryptoConfigurations = cryptoConfigurations;
    }

    public static CryptoWrapper of(ImmutableList<CryptoConfiguration> cryptoConfigurations) {
        return new CryptoWrapper(cryptoConfigurations);
    }

    public Optional<CryptoConfiguration> getLatestCryptoConfiguration() {
        return cryptoConfigurations.stream()
                .max(Comparator.comparing(t -> t.getCryptoConfigurationId().getKeyId()));
    }

    public Optional<byte[]> getCryptoKeyByKeyId(int keyId) {
        return cryptoConfigurations.stream()
                .filter(t -> Objects.equals(t.getKeyId(), keyId))
                .map(CryptoConfiguration::getDecodedKey)
                .findFirst();
    }

    public Optional<String> getClientName() {
        return cryptoConfigurations.stream()
                .map(CryptoConfiguration::getCryptoConfigurationId)
                .map(CryptoConfigurationId::getClientName)
                .findFirst();
    }
}
