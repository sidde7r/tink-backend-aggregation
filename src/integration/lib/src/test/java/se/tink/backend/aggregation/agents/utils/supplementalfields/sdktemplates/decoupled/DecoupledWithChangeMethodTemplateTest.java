package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledWithChangeMethodData;

public class DecoupledWithChangeMethodTemplateTest {

    private static final String ICON_URL = "https://www.dummyurl.com";

    @Test
    public void shouldReturnFilledDecoupledWithChangeMethodTemplate() {
        // given
        DecoupledWithChangeMethodData decoupledData =
                DecoupledWithChangeMethodData.builder()
                        .iconUrl(ICON_URL)
                        .text("shown instruction")
                        .buttonText("change method text")
                        .build();

        // when
        List<Field> supplementalFields =
                DecoupledWithChangeMethodTemplate.getTemplate(decoupledData);

        // then
        assertThat(supplementalFields).hasSize(4);
        assertTemplateField(supplementalFields.get(0));
        assertIconField(supplementalFields.get(1));
        assertTextField(supplementalFields.get(2));
        assertChangeMethodField(supplementalFields.get(3));
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

    private void assertTextField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("title");
        assertThat(field.getStyle()).isEqualTo("TEXT");
        assertThat(field.getType()).isEqualTo("TEXT");
        assertThat(field.getValue()).isEqualTo("shown instruction");
    }

    private void assertChangeMethodField(Field field) {
        assertThat(field).isNotNull();
        assertThat(field.getDescription()).isEqualTo("CHANGE_METHOD");
        assertThat(field.isImmutable()).isTrue();
        assertThat(field.getName()).isEqualTo("CHANGE_METHOD");
        assertThat(field.getType()).isEqualTo("CHANGE_METHOD");
        assertThat(field.getValue()).isEqualTo("change method text");
    }
}
