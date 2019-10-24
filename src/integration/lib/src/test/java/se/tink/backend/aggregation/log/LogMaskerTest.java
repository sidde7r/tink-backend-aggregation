package se.tink.backend.aggregation.log;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMaskerBuilder;

public class LogMaskerTest {

    @Test
    public void testIsWhiteListed() {
        LogMasker logMasker =
                LogMasker.builder()
                        .addStringMaskerBuilder(
                                new ClientConfigurationStringMaskerBuilder(
                                        Arrays.asList("true", "false", "222", "1", "5555")))
                        .build();

        String unmasked = "true2225555falsealfgoiangoiandg555adlkga222";
        String masked = logMasker.mask(unmasked);
        Assert.assertEquals(
                "Didn't mask sensitive values as expected.",
                "true222***MASKED***falsealfgoiangoiandg555adlkga222",
                masked);
    }
}
