package se.tink.backend.aggregation.provider.configuration.storage.module;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider.AgentCapabilitiesMapModel;
import static org.assertj.core.api.Assertions.assertThat;

public class ProviderFileModuleTest {
    ProviderFileModule module;

    @Before
    public void createModule(){
        module = new ProviderFileModule();
    }

    @Test
    public void loadProviderConfigurationFromJsonTest() throws Exception{
        Map<String, ProviderConfiguration> map = module.loadProviderConfigurationFromJson();
        Assert.assertNotNull(map);
        Assert.assertFalse(map.isEmpty());
    }

    @Test
    public void loaderLoadsOverrideFileSuccessTest() throws IOException{
        module.loadProviderOverrideOnClusterFromJson();
    }

    @Test
    public void loadEnabledProvidersEnsureNotEmptyTest() throws IOException{
        Map<String, Set<String>> clusterProvider = module.loadEnabledProvidersOnClusterFromJson();
        List<Set<String>> emptyProviderSets = clusterProvider.values().stream()
                .filter(Set::isEmpty)
                .collect(Collectors.toList());
        Assert.assertTrue(emptyProviderSets.isEmpty());
    }

    @Test
    public void whenProvideAgentCapabilities_ensureNotEmpty() throws IOException {
        Map<String, Set<ProviderConfiguration.Capability>> agentCapabilities = module.provideAgentCapabilities();
        assertThat(agentCapabilities).isNotEmpty();
    }
}
