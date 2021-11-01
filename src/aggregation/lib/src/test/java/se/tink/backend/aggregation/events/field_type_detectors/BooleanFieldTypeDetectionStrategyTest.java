package se.tink.backend.aggregation.events.field_type_detectors;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.BooleanFieldTypeDetectionStrategy;

public class BooleanFieldTypeDetectionStrategyTest {

    private final BooleanFieldTypeDetectionStrategy sut = new BooleanFieldTypeDetectionStrategy();

    @Test
    public void shouldDetectIfFieldIsABooleanField1() {
        assertTrue(sut.isTypeMatched(Collections.emptyList(), "true", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsNotABooleanField1() {
        assertFalse(sut.isTypeMatched(Collections.emptyList(), "dummy-value", JsonNodeType.STRING));
    }
}
