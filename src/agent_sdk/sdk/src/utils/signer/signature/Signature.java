package se.tink.agent.sdk.utils.signer.signature;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Signature {
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder();

    private final byte[] signatureBytes;

    private Signature(byte[] signatureBytes) {
        this.signatureBytes = signatureBytes;
    }

    public byte[] getBytes() {
        return this.signatureBytes;
    }

    public String getString(Charset charset) {
        return new String(this.signatureBytes, charset);
    }

    public String getString() {
        return getString(StandardCharsets.US_ASCII);
    }

    public String getBase64Encoded() {
        return BASE64_ENCODER.encodeToString(this.signatureBytes);
    }

    public String getBase64UrlEncoded() {
        return BASE64_URL_ENCODER.encodeToString(this.signatureBytes);
    }

    public static Signature create(byte[] signatureBytes) {
        return new Signature(signatureBytes);
    }
}
