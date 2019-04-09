package se.tink.libraries.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.data.Offset;
import org.junit.Test;

public class GenericApplicationFieldGroupTest {
    @Test
    public void getFieldAsIntegerWithComma() {
        GenericApplicationFieldGroup genericApplicationFieldGroup =
                new GenericApplicationFieldGroup();
        genericApplicationFieldGroup.setFields(ImmutableMap.of("key", "1,5"));

        Integer value = genericApplicationFieldGroup.getFieldAsInteger("key");

        // Fallback to value without decimals
        assertThat(value).isEqualTo(1);
    }

    @Test
    public void getFieldAsDoubleWithComma() {
        GenericApplicationFieldGroup genericApplicationFieldGroup =
                new GenericApplicationFieldGroup();
        genericApplicationFieldGroup.setFields(ImmutableMap.of("key", "1,5"));

        Double value = genericApplicationFieldGroup.getFieldAsDouble("key");
        assertThat(value).isEqualTo(1.5, Offset.offset(0.0001));
    }
}
