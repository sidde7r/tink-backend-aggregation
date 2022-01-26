package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MapExtractorTest {

    private final String key;
    private Map<String, String> callbackData;

    public MapExtractorTest(String key) {
        this.key = key;
    }

    @Parameters
    public static String[] getKeysForEmptyOptional() {
        return new String[] {"dummyKey", null, "nullKey", "empty"};
    }

    @Before
    public void setUp() throws Exception {
        callbackData = new HashMap<>();
        callbackData.put("key", "value");
        callbackData.put("empty", "");
        callbackData.put("nullKey", null);
    }

    @Test
    public void shouldThrowNullPointerExceptionWithProperMessageWhenNullMapProvided() {
        assertThatThrownBy(() -> MapExtractor.getCallbackElement(null, "key"))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("Callback data cannot be null!");
    }

    @Test
    public void shouldReturnEmptyOptionalForProvidedKeys() {
        Optional<String> element = MapExtractor.getCallbackElement(callbackData, key);
        assertThat(element).isEmpty();
    }

    @Test
    public void shouldReturnProperElement() {
        Optional<String> element = MapExtractor.getCallbackElement(callbackData, "key");
        assertThat(element).isPresent().hasValue("value");
    }
}
