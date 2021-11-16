package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.encoding.EncodingUtils;

public class DynamicVectorTest {
    private static final String DYNAMIC_VECTOR_AS_HEX =
            "00005FDF47AA6B0000000000000000004D01F2BD471260C4201A7126BADF62726786D8FB49E2D82D6CB051E572088035641ACFB6DA59";

    @Test
    public void testCalculateLogonId() {
        byte[] dynamicVector = EncodingUtils.decodeHexString(DYNAMIC_VECTOR_AS_HEX);

        String logonId = DynamicVector.calculateLogonId(dynamicVector);
        Assert.assertEquals("LogonId mismatch.", "4696683", logonId);
    }

    @Test
    public void testDecryptDynamicVectorKey() {
        byte[] key = EncodingUtils.decodeHexString("f3ff3eaf401cf61bfd1a531619956d5d");
        byte[] dynamicVector = EncodingUtils.decodeHexString(DYNAMIC_VECTOR_AS_HEX);

        byte[] dynamicVectorKey = DynamicVector.decryptDynamicVectorKey(key, dynamicVector);
        Assert.assertEquals(
                "DynamicVectorKey mismatch.",
                "e61bf5f0afc4e89536b0fb07bf3b788c",
                EncodingUtils.encodeHexAsString(dynamicVectorKey));
    }
}
