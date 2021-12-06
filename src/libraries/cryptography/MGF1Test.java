package se.tink.libraries.cryptography;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.encoding.EncodingUtils;

public class MGF1Test {

    private static final String MASK = "c8efc25c440ab641132265749b539ebdbbda76c1";
    private static final String SEED = "13579";

    private static final String EXPECTED = "AtBBM27yvFkmhMJh7gC0I77zZtE=";

    @Test
    public void testSha1Mask() {
        byte[] maskedSeed = MGF1.generateMaskSHA1(SEED.getBytes(), MASK.length() / 2);

        Assert.assertArrayEquals(EncodingUtils.decodeBase64String(EXPECTED), maskedSeed);
    }
}
