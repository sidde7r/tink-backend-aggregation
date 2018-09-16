package se.tink.backend.aggregation.provider.configuration.storage.module;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void testLoaderLoadsFileSuccess() throws IOException{
        module.loadClusterSpecificProviderConfigurationFromJson();
    }

    @Test
    public void testLoadEnabledProviders() throws IOException{
        Map<String, List<String>> clusterProvider = module.loadClusterEnabledProviderNamesFromJson();
        List<List<String>> emptyProviders = clusterProvider.values().stream()
                .filter(List::isEmpty)
                .collect(Collectors.toList());
        Assert.assertTrue(emptyProviders.isEmpty());
    }
}
