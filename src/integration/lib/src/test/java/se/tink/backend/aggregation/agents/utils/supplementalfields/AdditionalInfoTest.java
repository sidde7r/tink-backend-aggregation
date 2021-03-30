package se.tink.backend.aggregation.agents.utils.supplementalfields;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.supplementalfields.AdditionalInfo.LayoutTypes;

public class AdditionalInfoTest {

    @Test
    public void shouldSerializeOnlyNonNull() {
        // given
        AdditionalInfo additionalInfo =
                AdditionalInfo.builder().layoutType(LayoutTypes.INSTRUCTIONS).build();
        // when
        String serializedValue = additionalInfo.serialize();
        // then
        assertThat(serializedValue).isEqualTo("{\"layoutType\":\"INSTRUCTIONS\"}");
    }

    @Test
    public void shouldSerializeInstructionList() {
        // give
        List<String> instructions = Arrays.asList("Instr1", "Instr2");
        AdditionalInfo additionalInfo = AdditionalInfo.builder().instructions(instructions).build();
        // when
        String serializedValue = additionalInfo.serialize();
        // then
        assertThat(serializedValue).isEqualTo("{\"instructions\":[\"Instr1\",\"Instr2\"]}");
    }
}
