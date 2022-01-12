package se.tink.backend.aggregation.wrappers;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;

@Slf4j
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

    public byte[] getCryptoKeyByKeyId(int keyId) {
        List<CryptoConfiguration> cryptoConfigs =
                cryptoConfigurations.stream()
                        .filter(t -> Objects.equals(t.getKeyId(), keyId))
                        .collect(Collectors.toList());

        cryptoConfigs.forEach(
                cc ->
                        log.info(
                                "Crypto config with key id: {} and client name: {}",
                                cc.getKeyId(),
                                cc.getCryptoConfigurationId().getClientName()));

        List<byte[]> keyList =
                cryptoConfigs.stream()
                        .map(CryptoConfiguration::getDecodedKey)
                        .collect(Collectors.toList());

        return keyList.stream()
                .findAny()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format("Could not find key with id: %s", keyId)));
    }

    public Optional<String> getClientName() {
        return cryptoConfigurations.stream()
                .map(CryptoConfiguration::getCryptoConfigurationId)
                .map(CryptoConfigurationId::getClientName)
                .findFirst();
    }
}
