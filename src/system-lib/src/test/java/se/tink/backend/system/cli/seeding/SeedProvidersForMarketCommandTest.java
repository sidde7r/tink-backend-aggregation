package se.tink.backend.system.cli.seeding;

import org.junit.Assert;
import org.junit.Test;

public class SeedProvidersForMarketCommandTest {

    @Test
    public void testEscapeMarketArgument() {
        SeedProvidersForMarketCommand cmd = new SeedProvidersForMarketCommand();

        Assert.assertEquals("abc", cmd.escapeMarket("/abc"));
        Assert.assertEquals("abcabc", cmd.escapeMarket("/abc/abc"));
        Assert.assertEquals("aabcabc", cmd.escapeMarket("a/abc/abc"));
        Assert.assertEquals("abcabc", cmd.escapeMarket("../abc/abc"));
        Assert.assertEquals("Fabc", cmd.escapeMarket("%2Fabc"));
    }
}
