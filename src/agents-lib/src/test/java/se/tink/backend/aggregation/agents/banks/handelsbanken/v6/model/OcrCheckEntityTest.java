package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

public class OcrCheckEntityTest {
    @Test
    public void deserialize() throws IOException {
        OcrCheckEntity expected = new OcrCheckEntity();
        expected.setType(OcrCheckEntity.OcrCheckType.OCR_FIXED_LENGTH);
        expected.setRefLength1(1);
        expected.setRefLength2(2);

        ObjectMapper objectMapper = new ObjectMapper();
        OcrCheckEntity deserialized = objectMapper.readValue("{\n"
                + "  \"refLength1\": 1,\n"
                + "  \"refLength2\": 2,\n"
                + "  \"type\": \"OCR_FIXED_LENGTH\"\n"
                + "}", OcrCheckEntity.class);

        assertThat(deserialized).isEqualTo(expected);
    }

    @Test
    public void serialize() throws IOException {
        OcrCheckEntity nonSerialized = new OcrCheckEntity();
        nonSerialized.setType(OcrCheckEntity.OcrCheckType.OCR_FIXED_LENGTH);
        nonSerialized.setRefLength1(1);
        nonSerialized.setRefLength2(2);

        ObjectMapper objectMapper = new ObjectMapper();
        String serialized = objectMapper.writeValueAsString(nonSerialized);

        assertThat(serialized).isEqualTo("{\"refLength1\":1,\"refLength2\":2,\"type\":\"OCR_FIXED_LENGTH\"}");
    }

    /**
     * OCR configuration tests based on checking out how the Android SHB app works when adding a new payment
     */
    @Test
    public void withControlDigitMeansOnlyLuhnCheck() {
        OcrCheckEntity ocrCheckEntity = new OcrCheckEntity();
        ocrCheckEntity.setType(OcrCheckEntity.OcrCheckType.OCR_WITH_CONTROL_DIGIT);

        OcrValidationConfiguration expected = OcrValidationConfiguration.hardOcrVariableLength();
        assertThat(ocrCheckEntity.getValidationConfiguration()).isEqualTo(expected);
    }

    @Test
    public void variableLengthMeansWithLuhnAndLengthCheck() {
        OcrCheckEntity ocrCheckEntity = new OcrCheckEntity();
        ocrCheckEntity.setType(OcrCheckEntity.OcrCheckType.OCR_VARIABLE_LENGTH);

        OcrValidationConfiguration expected = OcrValidationConfiguration.hardOcrVariableLengthWithLengthCheck();
        assertThat(ocrCheckEntity.getValidationConfiguration()).isEqualTo(expected);
    }

    @Test
    public void fixedLengthMeansFixedLength() {
        OcrCheckEntity ocrCheckEntity = new OcrCheckEntity();
        ocrCheckEntity.setRefLength1(10);
        ocrCheckEntity.setRefLength2(11);
        ocrCheckEntity.setType(OcrCheckEntity.OcrCheckType.OCR_FIXED_LENGTH);

        OcrValidationConfiguration expected = OcrValidationConfiguration.hardOcrFixedLength(10, 11);
        assertThat(ocrCheckEntity.getValidationConfiguration()).isEqualTo(expected);
    }

    @Test
    public void messageOrOcrMeansSoft() {
        OcrCheckEntity ocrCheckEntity = new OcrCheckEntity();
        ocrCheckEntity.setType(OcrCheckEntity.OcrCheckType.MESSAGE_OR_OCR);

        OcrValidationConfiguration expected = OcrValidationConfiguration.softOcr();
        assertThat(ocrCheckEntity.getValidationConfiguration()).isEqualTo(expected);
    }

    @Test
    public void messageMeansNoOcr() {
        OcrCheckEntity ocrCheckEntity = new OcrCheckEntity();
        ocrCheckEntity.setType(OcrCheckEntity.OcrCheckType.MESSAGE);

        OcrValidationConfiguration expected = OcrValidationConfiguration.noOcr();
        assertThat(ocrCheckEntity.getValidationConfiguration()).isEqualTo(expected);
    }
}
