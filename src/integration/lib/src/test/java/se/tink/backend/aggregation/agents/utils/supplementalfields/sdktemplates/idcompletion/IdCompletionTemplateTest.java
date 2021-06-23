package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.idcompletion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonInput;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonPositionalInput;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.InGroup;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.idcompletion.dto.IdCompletionData;

public class IdCompletionTemplateTest {

    private static final String IMAGE_URL = "https://www.dummyurl.com";

    @Test
    public void shouldReturnFilledIdCompletionTemplate() {
        // given
        CommonInput commonInput =
                CommonInput.builder()
                        .description("desc")
                        .inputFieldHelpText("Help Text")
                        .inputFieldMaxLength(3)
                        .inputFieldMinLength(3)
                        .inputFieldPattern("\\%s")
                        .inputFieldPatternError("Pattern Error Message")
                        .sensitive(true)
                        .build();

        CommonPositionalInput positionalInput1 =
                CommonPositionalInput.builder()
                        .description("desc pi1")
                        .inputFieldHelpText("Help Text")
                        .inputFieldMaxLength(3)
                        .inputFieldMinLength(3)
                        .inputFieldPattern("\\%s")
                        .inputFieldPatternError("Pattern Error Message")
                        .inGroup(InGroup.builder().group("Test").oneOf(true).build())
                        .positionOfFieldsToHide(Arrays.asList(0, 2))
                        .build();

        CommonPositionalInput positionalInput2 =
                CommonPositionalInput.builder()
                        .description("desc pi2")
                        .inputFieldHelpText("Help Text")
                        .inputFieldMaxLength(3)
                        .inputFieldMinLength(3)
                        .inputFieldPattern("\\%s")
                        .inputFieldPatternError("Pattern Error Message")
                        .inGroup(InGroup.builder().group("Test").oneOf(true).build())
                        .positionOfFieldsToHide(Arrays.asList(1))
                        .build();

        List<CommonPositionalInput> positionalInputs =
                Arrays.asList(positionalInput1, positionalInput2);

        IdCompletionData idCompletionData =
                IdCompletionData.builder()
                        .colorHex("HEX")
                        .identityHintText("Hint Text")
                        .identityHintImage(IMAGE_URL)
                        .passwordInput(commonInput)
                        .title("Title Value")
                        .identifications(positionalInputs)
                        .build();

        // when
        List<Field> supplementalFields = IdCompletionTemplate.getTemplate(idCompletionData);

        // then
        assertThat(supplementalFields).hasSize(7);
        assertTemplateField(supplementalFields.get(0));
        assertColorField(supplementalFields.get(1));
        assertTitleField(supplementalFields.get(2));
        assertIdentityHintField(supplementalFields.get(3));
        assertPasswordInputField(supplementalFields.get(4));
        assertFirstChooseField(supplementalFields.get(5));
        assertSecondChooseField(supplementalFields.get(6));
    }

    private void assertTemplateField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("TEMPLATE");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("TEMPLATE");
        assertThat(field.getType()).isEqualTo("TEMPLATE");
        assertThat(field.getValue()).isEqualTo("ID_COMPLETION");
    }

    private void assertColorField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("COLOR");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("color");
        assertThat(field.getType()).isEqualTo("COLOR");
        assertThat(field.getValue()).isEqualTo("HEX");
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

    private void assertIdentityHintField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("Hint Text");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("identityHint");
        assertThat(field.getStyle()).isEqualTo("IDENTITY_HINT");
        assertThat(field.getType()).isEqualTo("ICON");
        assertThat(field.getValue()).isEqualTo(IMAGE_URL);
    }

    private void assertPasswordInputField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("desc");
        assertThat(field.getHelpText()).isEqualTo("Help Text");
        assertThat(field.getHint()).isEqualTo("NNN");
        assertThat(field.getName()).isEqualTo("password");
        assertThat(field.getPattern()).isEqualTo("\\%s");
        assertThat(field.getPatternError()).isEqualTo("Pattern Error Message");
        assertThat(field.isSensitive()).isTrue();
        assertThat(field.getStyle()).isEqualTo("INPUT");
        assertThat(field.getType()).isEqualTo("INPUT");
        assertThat(field.getValue()).isNull();
    }

    private void assertFirstChooseField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("desc pi1");
        assertThat(field.getGroup()).isEqualTo("Test");
        assertThat(field.getHelpText()).isEqualTo("Help Text");
        assertThat(field.getHint()).isEqualTo("XNX");
        assertThat(field.getMaxLength()).isEqualTo(3);
        assertThat(field.getMinLength()).isEqualTo(3);
        assertThat(field.getName()).isEqualTo("identifications0");
        assertThat(field.isOneOf()).isTrue();
        assertThat(field.getPattern()).isEqualTo("\\%s");
        assertThat(field.getPatternError()).isEqualTo("Pattern Error Message");
        assertThat(field.isSensitive()).isFalse();
        assertThat(field.getStyle()).isEqualTo("POSITIONAL_INPUT");
        assertThat(field.getType()).isEqualTo("INPUT");
        assertThat(field.getValue()).isNull();
    }

    private void assertSecondChooseField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("desc pi2");
        assertThat(field.getGroup()).isEqualTo("Test");
        assertThat(field.getHelpText()).isEqualTo("Help Text");
        assertThat(field.getHint()).isEqualTo("NXN");
        assertThat(field.getMaxLength()).isEqualTo(3);
        assertThat(field.getMinLength()).isEqualTo(3);
        assertThat(field.getName()).isEqualTo("identifications1");
        assertThat(field.isOneOf()).isTrue();
        assertThat(field.getPattern()).isEqualTo("\\%s");
        assertThat(field.getPatternError()).isEqualTo("Pattern Error Message");
        assertThat(field.isSensitive()).isFalse();
        assertThat(field.getStyle()).isEqualTo("POSITIONAL_INPUT");
        assertThat(field.getType()).isEqualTo("INPUT");
        assertThat(field.getValue()).isNull();
    }
}
