package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass;

import java.util.Arrays;
import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.DataUtils;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.XfadUtils;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class StaticVector {
    private final byte[] vector;

    private StaticVector(byte[] vector) {
        this.vector = vector;
    }

    public static StaticVector createFromXfad(byte[] xfad) {
        return new StaticVector(XfadUtils.getStaticVector(xfad));
    }

    public static StaticVector createFromXfad(String xfadHex) {
        byte[] xfad = EncodingUtils.decodeHexString(xfadHex);
        return new StaticVector(XfadUtils.getStaticVector(xfad));
    }

    public Optional<byte[]> getField(int fieldType, int index) {
        // The static vector is a TLV structure.
        // 1 byte type
        // 1 byte length
        // n bytes data
        int pos = 0;
        while (pos < vector.length) {
            int type = vector[pos++] & 0xff;
            if (pos >= vector.length) {
                break;
            }

            int length = vector[pos++] & 0xff;
            if (pos >= vector.length) {
                break;
            }

            if (type == fieldType && (index-- == 0)) {
                return Optional.of(Arrays.copyOfRange(vector, pos, pos + length));
            }

            pos += length;
        }

        return Optional.empty();
    }

    public byte[] getMandatoryField(int fieldType) {
        return getField(fieldType, 0)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format(
                                                "Could not find StaticVector field type: %d",
                                                fieldType)));
    }

    public Optional<byte[]> getField(int fieldType) {
        return getField(fieldType, 0);
    }

    public Optional<Integer> getFieldAsInt(int fieldType) {
        Optional<byte[]> bValue = getField(fieldType, 0);
        return bValue.map(DataUtils::bytesToInt);
    }

    public Optional<Long> getFieldAsLong(int fieldType) {
        Optional<byte[]> bValue = getField(fieldType, 0);
        return bValue.map(DataUtils::bytesToLong);
    }

    // For debugging purposes only
    public String vectorToString() {
        StringBuilder sb = new StringBuilder();

        int pos = 0;
        while (pos < vector.length) {
            int type = vector[pos++] & 0xff;
            if (pos >= vector.length) {
                break;
            }

            int length = vector[pos++] & 0xff;
            if (pos >= vector.length) {
                break;
            }

            if (length + pos > vector.length) {
                break;
            }

            byte[] data = Arrays.copyOfRange(vector, pos, pos + length);

            sb.append("type: ");
            sb.append(type);
            sb.append("\n");
            sb.append("data: ");
            sb.append(EncodingUtils.encodeHexAsString(data));
            sb.append("\n");

            pos += length;
        }

        return sb.toString();
    }
}
