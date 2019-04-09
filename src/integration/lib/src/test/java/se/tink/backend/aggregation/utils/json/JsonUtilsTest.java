package se.tink.backend.aggregation.utils.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class JsonUtilsTest {
    @Test
    public void testIsValidJson() {
        assertThat(JsonUtils.isValidJson("{'accounts' : 'foo-bar'}")).isFalse();
    }

    @Test
    public void testInvalidJson() {
        assertThat(JsonUtils.isValidJson("<html>...</html>")).isFalse();
    }
}
