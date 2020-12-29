package se.tink.backend.aggregation.utils.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Assert;
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

    @Test
    public void testEscapeNotSpecialSingleBackslashes() {
        // given
        final String jsonToFix =
                "{\"valueToReplace1\": \"value\\ \",\"bacskapceValue\": \"value\\b \",\"formFeedValue\": \"value\\f\",\"newLineValue\": \"value\\n\",\"carriageReturnValue\": \"value\\r\",\"tabValue\": \"value\\t\",\"doubleQuote\": \"value\\\"\",\"backslashValue\": \"value\\\\\",\"valueToReplace2\": \"value\\ \"}";
        final String fixedJson =
                "{\"valueToReplace1\": \"value\\\\ \",\"bacskapceValue\": \"value\\b \",\"formFeedValue\": \"value\\f\",\"newLineValue\": \"value\\n\",\"carriageReturnValue\": \"value\\r\",\"tabValue\": \"value\\t\",\"doubleQuote\": \"value\\\"\",\"backslashValue\": \"value\\\\\",\"valueToReplace2\": \"value\\\\ \"}";
        // when
        String result = JsonUtils.escapeNotSpecialSingleBackslashes(jsonToFix);
        // then
        Assert.assertEquals(fixedJson, result);
    }
}
