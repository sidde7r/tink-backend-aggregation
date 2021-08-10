package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.cardreader;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.cardreader.dto.CardReaderData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonInput;

public class CardReaderTemplateTest {

    @Test
    public void shouldReturnFilledCardReaderTemplate() {
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

        CardReaderData cardReaderData =
                CardReaderData.builder()
                        .instructionFieldDescription("instruction")
                        .instructions(Arrays.asList("Instr 1", "Instr 2", "Instr 3"))
                        .input(commonInput)
                        .secondFactorDescription("2FA desc")
                        .secondFactorValue("2FA val")
                        .build();

        // when
        List<Field> supplementalFields = CardReaderTemplate.getTemplate(cardReaderData);

        // then
        assertThat(supplementalFields).hasSize(4);
        assertTemplateField(supplementalFields.get(0));
        assertInstructionField(supplementalFields.get(1));
        assertInputField(supplementalFields.get(2));
        assertInstructionListField(supplementalFields.get(3));
    }

    private void assertTemplateField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("TEMPLATE");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("TEMPLATE");
        assertThat(field.getType()).isEqualTo("TEMPLATE");
        assertThat(field.getValue()).isEqualTo("CARD_READER");
    }

    private void assertInstructionField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getAdditionalInfo()).isEqualTo("{\"layoutType\":\"INSTRUCTIONS\"}");
        assertThat(field.getDescription()).isEqualTo("2FA desc");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("instruction");
        assertThat(field.getStyle()).isEqualTo("INSTRUCTION");
        assertThat(field.getType()).isEqualTo("TEXT");
        assertThat(field.getValue()).isEqualTo("2FA val");
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

    private void assertInstructionListField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("instruction");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("instructionList");
        assertThat(field.getStyle()).isEqualTo("ORDERED_LIST");
        assertThat(field.getType()).isEqualTo("TEXT");
        assertThat(field.getValue()).isEqualTo("[\"Instr 1\",\"Instr 2\",\"Instr 3\"]");
    }
}
