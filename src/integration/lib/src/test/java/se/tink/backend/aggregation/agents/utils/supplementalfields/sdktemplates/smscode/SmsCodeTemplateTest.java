package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.smscode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonInput;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.smscode.dto.SmsCodeData;

public class SmsCodeTemplateTest {
    private static final String ICON_URL = "https://www.dummyurl.com";

    @Test
    public void shouldReturnFilledSmsCodeTemplate() {
        // given
        CommonInput commonInput =
                CommonInput.builder()
                        .description("desc")
                        .inputFieldHelpText("Help Text")
                        .inputFieldMaxLength(3)
                        .inputFieldMinLength(3)
                        .inputFieldPattern("\\%s")
                        .inputFieldPatternError("Pattern Error Message")
                        .build();

        SmsCodeData smsCodeData =
                SmsCodeData.builder()
                        .iconUrl(ICON_URL)
                        .title("Title Value")
                        .instructions(Arrays.asList("Instr 1", "Instr 2", "Instr 3"))
                        .input(commonInput)
                        .build();

        // when
        List<Field> supplementalFields = SmsCodeTemplate.getTemplate(smsCodeData);

        // then
        assertThat(supplementalFields).hasSize(5);
        assertTemplateField(supplementalFields.get(0));
        assertIconField(supplementalFields.get(1));
        assertTitleField(supplementalFields.get(2));
        assertInputField(supplementalFields.get(3));
        assertInstructionField(supplementalFields.get(4));
    }

    private void assertTemplateField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("TEMPLATE");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("TEMPLATE");
        assertThat(field.getType()).isEqualTo("TEMPLATE");
        assertThat(field.getValue()).isEqualTo("SMS_CODE");
    }

    private void assertIconField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("ICON");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("icon");
        assertThat(field.getType()).isEqualTo("ICON");
        assertThat(field.getValue()).isEqualTo(ICON_URL);
    }

    private void assertTitleField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("TITLE");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("title");
        assertThat(field.getStyle()).isEqualTo("TITLE");
        assertThat(field.getType()).isEqualTo("TEXT");
        assertThat(field.getValue()).isEqualTo("Title Value");
    }

    private void assertInputField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("desc");
        assertThat(field.getHelpText()).isEqualTo("Help Text");
        assertThat(field.getHint()).isEqualTo("NNN");
        assertThat(field.getName()).isEqualTo("input");
        assertThat(field.getPattern()).isEqualTo("\\%s");
        assertThat(field.getPatternError()).isEqualTo("Pattern Error Message");
        assertThat(field.getStyle()).isEqualTo("INPUT");
        assertThat(field.getType()).isEqualTo("INPUT");
        assertThat(field.getValue()).isNull();
    }

    private void assertInstructionField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("ORDERED_LIST");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("instructions");
        assertThat(field.getStyle()).isEqualTo("ORDERED_LIST");
        assertThat(field.getType()).isEqualTo("TEXT");
        assertThat(field.getValue()).isEqualTo("[\"Instr 1\",\"Instr 2\",\"Instr 3\"]");
    }
}
