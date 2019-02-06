package se.tink.backend.aggregation.wrappers;

import com.google.common.collect.ImmutableList;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;
import static org.assertj.core.api.Assertions.assertThat;

public class CryptoWrapperTest {
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final int DEFAULT_CRYPTO_CONFIGURATIONS = 10;
    private static final int NUMBER_BETWEEN_ZERO_AND_DEFAULT = 5;
    private static final String CLIENT_NAME = "Client Name";
    private static final String KEY_STRING_FORMAT = "Key %s";
    private CryptoWrapper cryptoWrapper;

    @Before
    public void setUp() {
        cryptoWrapper = createCryptoWrapper(DEFAULT_CRYPTO_CONFIGURATIONS);
    }

    @Test
    public void ensureClientNameIsPresent_whenListNotEmpty() {
        Optional<String> clientName = cryptoWrapper.getClientName();

        assertThat(clientName.isPresent()).isTrue();
    }

    @Test
    public void ensureClientNameIsNotPresent_whenListEmpty() {
        Optional<String> clientName = createCryptoWrapper(0).getClientName();

        assertThat(clientName.isPresent()).isFalse();
    }

    @Test
    public void ensureIsPresent_whenListNotEmpty() {
        Optional<CryptoConfiguration> latestCryptoConfiguration = cryptoWrapper.getLatestCryptoConfiguration();

        assertThat(latestCryptoConfiguration.isPresent()).isTrue();
    }

    @Test
    public void ensureNotPresent_whenListEmpty() {
        CryptoWrapper cryptoWrapper = createCryptoWrapper(0);

        Optional<CryptoConfiguration> latestCryptoConfiguration = cryptoWrapper.getLatestCryptoConfiguration();

        assertThat(latestCryptoConfiguration.isPresent()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureWhenListEmpty_cryptoKeyByIdNotPresent() {
        createCryptoWrapper(0).getCryptoKeyByKeyId(0);
    }

    @Test
    public void ensureWhenRandomIntBetweenOneAndTen_cryptoKeyByKeyIdIsPresent() {
        byte[] cryptoKeyByKeyId = cryptoWrapper.getCryptoKeyByKeyId(NUMBER_BETWEEN_ZERO_AND_DEFAULT);

        assertThat(Objects.nonNull(cryptoKeyByKeyId)).isTrue();
    }

    @Test
    public void ensureClientName_equalsExpectedClientName() {
        Optional<String> clientName = cryptoWrapper.getClientName();

        assertThat(clientName.get()).isEqualTo(CLIENT_NAME);
    }

    @Test
    public void ensureLatestCryptoConfigurationKeyId_equalToNumberOfDefaultCryptoConfigurations() {
        Optional<CryptoConfiguration> cryptoConfiguration = cryptoWrapper.getLatestCryptoConfiguration();

        // Since there is a test that tests that there actually is a latest crypto configuration there is no
        // need for if-statements before doing get on the cryptoConfiguration.
        assertThat(cryptoConfiguration.get().getKeyId()).isEqualTo(DEFAULT_CRYPTO_CONFIGURATIONS);
    }

    @Test
    public void ensureGetCryptoKeyByKeyId_returnsCorrectCryptoKeyByKeyId() {
        byte[] cryptoKeyByKeyId = cryptoWrapper.getCryptoKeyByKeyId(NUMBER_BETWEEN_ZERO_AND_DEFAULT);

        assertThat(new String(cryptoKeyByKeyId)).isEqualTo(
                String.format(KEY_STRING_FORMAT, NUMBER_BETWEEN_ZERO_AND_DEFAULT));
    }

    private CryptoWrapper createCryptoWrapper(int totalItems) {
        return CryptoWrapper.of(createCryptoConfigurations(totalItems));
    }

    private ImmutableList<CryptoConfiguration> createCryptoConfigurations(int totalItems) {
        ImmutableList.Builder<CryptoConfiguration> listBuilder = ImmutableList.builder();

        for (int item = 1; item <= totalItems; item++) {
            listBuilder.add(createCryptoConfiguration(CLIENT_NAME, item, String.format(KEY_STRING_FORMAT, item)));
        }

        return listBuilder.build();
    }

    private CryptoConfiguration createCryptoConfiguration(String clientName, int keyId, String key) {
        CryptoConfiguration cryptoConfiguration = new CryptoConfiguration();

        CryptoConfigurationId cryptoConfigurationId = new CryptoConfigurationId();

        cryptoConfigurationId.setClientName(clientName);
        cryptoConfigurationId.setKeyId(keyId);

        cryptoConfiguration.setCryptoConfigurationId(cryptoConfigurationId);
        cryptoConfiguration.setBase64encodedkey(BASE64_ENCODER.encodeToString(key.getBytes()));

        return cryptoConfiguration;
    }
}
