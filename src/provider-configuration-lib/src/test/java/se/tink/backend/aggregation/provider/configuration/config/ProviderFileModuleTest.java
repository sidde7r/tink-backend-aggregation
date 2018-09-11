package se.tink.backend.aggregation.provider.configuration.config;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.core.ProviderConfiguration;

import java.io.IOException;
import java.util.Map;

public class ProviderFileModuleTest {

    @Test
    public void loadProviderConfigurationFromJsonTest() throws IOException{
        Map<String, ProviderConfiguration> map = new ProviderFileModule().loadProviderConfigurationFromJson();
        Assert.assertNotNull(map);
        Assert.assertFalse(map.isEmpty());
    }
}
