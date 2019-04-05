package se.tink.backend.aggregation.provider.configuration.storage.module;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationStorage;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;

public class ProviderFileModuleTest {
    ProviderFileModule module;

    @Before
    public void createModule(){
        module = new ProviderFileModule();
    }

    @Test
    public void loadProviderConfigurationFromJsonTest() throws Exception{
        Map<String, ProviderConfigurationStorage> map = module.loadProviderConfigurationFromJson();
        assertThat(map).isNotNull();
        assertThat(map).isNotEmpty();
    }

    @Test
    public void loaderLoadsOverrideFileSuccessTest() throws IOException{
        module.loadProviderOverrideOnClusterFromJson();
    }

    @Test
    public void loadEnabledProvidersEnsureNotEmptyTest() throws IOException{
        Map<String, Set<String>> clusterProvider = module.loadEnabledProvidersOnClusterFromJson();

        Map<String, Set<String>> collect = clusterProvider.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(collect).isEmpty();
    }

    @Test
    public void whenProvideAgentCapabilities_ensureNotEmpty() throws IOException {
        Map<String, Set<ProviderConfigurationStorage.Capability>> agentCapabilities = module.provideAgentCapabilities();
        assertThat(agentCapabilities).isNotEmpty();
    }
}
