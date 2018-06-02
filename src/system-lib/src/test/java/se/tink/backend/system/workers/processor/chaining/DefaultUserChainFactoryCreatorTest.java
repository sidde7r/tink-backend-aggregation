package se.tink.backend.system.workers.processor.chaining;

import org.junit.Assert;
import org.junit.Test;

public class DefaultUserChainFactoryCreatorTest {

    @Test
    public void testEmptyStringPrefix() throws Exception {
        Assert.assertTrue("3173bf403c914e4fb0fdebecefa6c14f".startsWith(""));
    }
}
