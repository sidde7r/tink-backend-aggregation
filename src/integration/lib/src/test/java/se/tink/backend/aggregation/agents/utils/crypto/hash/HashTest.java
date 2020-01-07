package se.tink.backend.aggregation.agents.utils.crypto.hash;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class HashTest {
    private static final String EXPECTED = "7f9a6871b86f40c330132c4fc42cda59";

    @Test
    public void testMd5() {
        byte[] hash = Hash.md5("tinkerbell");

        Assert.assertArrayEquals(EncodingUtils.decodeHexString(EXPECTED), hash);
    }
}
