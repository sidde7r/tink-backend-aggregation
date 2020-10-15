package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto;

import java.math.BigInteger;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@Data
@AllArgsConstructor
public class SRP6ClientValues {

    private BigInteger publicValue;

    private BigInteger evidence;

    private BigInteger secret;

    public String getPublicValueString() {
        return EncodingUtils.encodeHexAsString(publicValue.toByteArray());
    }

    public String getEvidenceString() {
        return EncodingUtils.encodeHexAsString(evidence.toByteArray());
    }

    public byte[] getSecretBytes() {
        return removeLeadingZero(secret.toByteArray());
    }

    @Override
    public String toString() {
        return "SRP6ClientValues{"
                + "publicValue="
                + publicValue.toString(16)
                + ", evidence="
                + evidence.toString(16)
                + ", secret="
                + secret.toString(16)
                + '}';
    }

    private byte[] removeLeadingZero(byte[] bytes) {
        if (bytes[0] == 0) {
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }
}
