package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledData;

public class DecoupledTemplateTest {

    private static final String ICON_URL = "https://www.dummyurl.com";

    @Test
    public void shouldReturnFilledDecoupledTemplate() {
        // given
        DecoupledData decoupledData =
                DecoupledData.builder().iconUrl(ICON_URL).text("shown instruction").build();

        // when
        List<Field> supplementalFields = DecoupledTemplate.getTemplate(decoupledData);

        // then
        assertThat(supplementalFields).hasSize(3);
        assertTemplateField(supplementalFields.get(0));
        assertIconField(supplementalFields.get(1));
        assertTitleField(supplementalFields.get(2));
    }

    private void assertTemplateField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("TEMPLATE");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("TEMPLATE");
        assertThat(field.getType()).isEqualTo("TEMPLATE");
        assertThat(field.getValue()).isEqualTo("DECOUPLED");
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
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("title");
        assertThat(field.getStyle()).isEqualTo("TITLE");
        assertThat(field.getType()).isEqualTo("TEXT");
        assertThat(field.getValue()).isEqualTo("shown instruction");
        assertThat(field.getAdditionalInfo()).isEqualTo("{\"layoutType\":\"INSTRUCTIONS\"}");
    }
}
