package src.agent_sdk.sdk.test.utils.signer;

import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.sdk.utils.signer.signature.Signature;

public class SignatureTest {
    private static final byte[] RAW_DATA = {
        (byte) 0xfe, (byte) 0x01, (byte) 0x3f, (byte) 0x41, (byte) 0x00
    };
    private static final Signature SIGNATURE = Signature.create(RAW_DATA);

    @Test
    public void testGetBytes() {
        Assert.assertEquals(RAW_DATA, SIGNATURE.getBytes());
    }

    @Test
    public void testGetString() {
        Signature signature = Signature.create("foobar".getBytes());
        Assert.assertEquals("foobar", signature.getString());
    }

    @Test
    public void testBase64Encoded() {
        Assert.assertEquals("/gE/QQA=", SIGNATURE.getBase64Encoded());
    }

    @Test
    public void testBase64UrlEncoded() {
        Assert.assertEquals("_gE_QQA=", SIGNATURE.getBase64UrlEncoded());
    }
}
