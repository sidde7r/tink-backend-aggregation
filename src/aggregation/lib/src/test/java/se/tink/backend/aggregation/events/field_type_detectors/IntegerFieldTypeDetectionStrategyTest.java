package se.tink.backend.aggregation.events.field_type_detectors;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.IntegerFieldTypeDetectionStrategy;

public class IntegerFieldTypeDetectionStrategyTest {

    private final IntegerFieldTypeDetectionStrategy sut = new IntegerFieldTypeDetectionStrategy();

    @Test
    public void shouldDetectIfFieldIsAnIntegerField1() {
        assertTrue(sut.isTypeMatched(Collections.emptyList(), "1234", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsAnNonIntegerField1() {
        assertFalse(sut.isTypeMatched(Collections.emptyList(), "1234ab", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsAnNonIntegerField2() {
        assertFalse(sut.isTypeMatched(Collections.emptyList(), "2.0", JsonNodeType.STRING));
    }
}
