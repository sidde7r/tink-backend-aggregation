package se.tink.backend.aggregation.configuration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;

public class ConfigurationValidatorTest {
    private Map<String, ClientConfiguration> validClientConfigurations;
    private Map<String, AggregatorConfiguration> validAggregatorConfigurations;
    private Map<String, ClusterConfiguration> validClusterConfigurations;
    private CryptoConfigurationDao validCryptoConfigurationDao;

    @Before
    public void setUp() throws Exception {
        validClientConfigurations = getValidClientConfigurations();
        validAggregatorConfigurations = getValidAggregatorConfigurations(validClientConfigurations);
        validClusterConfigurations = getValidClusterConfigurations(validClientConfigurations);
        validCryptoConfigurationDao = getValidCryptoConfigurationDao(validClientConfigurations);
    }

    @Test
    public void whenAllConfigurationsAreValid_thenDoNotThrow() {
        // Arrange
        ConfigurationValidator configurationValidator =
                new ConfigurationValidator(
                        validClientConfigurations,
                        validClusterConfigurations,
                        validAggregatorConfigurations,
                        validCryptoConfigurationDao);

        // Act
        configurationValidator.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void whenClusterConfigurationsAreMissing_thenThrow() {
        // Arrange
        ConfigurationValidator configurationValidator =
                new ConfigurationValidator(
                        validClientConfigurations,
                        new HashMap<>(),
                        validAggregatorConfigurations,
                        validCryptoConfigurationDao);

        // Act
        configurationValidator.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void whenAggregatorConfigurationsAreMissing_thenThrow() {
        // Arrange
        ConfigurationValidator configurationValidator =
                new ConfigurationValidator(
                        validClientConfigurations,
                        validClusterConfigurations,
                        new HashMap<>(),
                        validCryptoConfigurationDao);

        // Act
        configurationValidator.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void whenCryptoConfigurationsAreMissing_thenThrow() {
        // Arrange
        CryptoConfigurationDao cryptoConfigurationDao = mock(CryptoConfigurationDao.class);
        when(cryptoConfigurationDao.getCryptoWrapperOfClientName(anyString()))
                .thenReturn(CryptoWrapper.of(ImmutableList.of(new CryptoConfiguration())));

        ConfigurationValidator configurationValidator =
                new ConfigurationValidator(
                        validClientConfigurations,
                        validClusterConfigurations,
                        validAggregatorConfigurations,
                        cryptoConfigurationDao);

        // Act
        configurationValidator.validate();
    }

    private CryptoConfigurationDao getValidCryptoConfigurationDao(
            Map<String, ClientConfiguration> validClientConfigurations) {
        CryptoConfigurationDao cryptoConfigurationDao = mock(CryptoConfigurationDao.class);

        for (ClientConfiguration clientConfiguration : validClientConfigurations.values()) {
            CryptoWrapper cryptoWrapper = mock(CryptoWrapper.class);
            when(cryptoWrapper.getClientName())
                    .thenReturn(Optional.of(clientConfiguration.getClientName()));
            when(cryptoConfigurationDao.getCryptoWrapperOfClientName(
                            clientConfiguration.getClientName()))
                    .thenReturn(cryptoWrapper);
        }

        return cryptoConfigurationDao;
    }

    private Map<String, ClientConfiguration> getValidClientConfigurations() {
        HashMap<String, ClientConfiguration> clientConfigurations = new HashMap<>();
        List<String> clientNames = Arrays.asList("clientName1", "clientName2");
        for (String clientName : clientNames) {
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setClusterId(clientName + "cluster");
            clientConfiguration.setAggregatorId(clientName + "aggregator");
            clientConfiguration.setClientName(clientName);
            clientConfigurations.put(clientName, clientConfiguration);
        }

        return clientConfigurations;
    }

    private Map<String, AggregatorConfiguration> getValidAggregatorConfigurations(
            Map<String, ClientConfiguration> clientConfigurations) {
        HashMap<String, AggregatorConfiguration> aggregatorConfigurations = new HashMap<>();
        for (ClientConfiguration clientConfiguration : clientConfigurations.values()) {
            aggregatorConfigurations.put(
                    clientConfiguration.getAggregatorId(), new AggregatorConfiguration());
        }

        return aggregatorConfigurations;
    }

    private Map<String, ClusterConfiguration> getValidClusterConfigurations(
            Map<String, ClientConfiguration> clientConfigurations) {
        HashMap<String, ClusterConfiguration> clusterConfigurations = new HashMap<>();
        for (ClientConfiguration clientConfiguration : clientConfigurations.values()) {
            clusterConfigurations.put(
                    clientConfiguration.getClusterId(), new ClusterConfiguration());
        }

        return clusterConfigurations;
    }
}
