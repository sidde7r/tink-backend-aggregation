package se.tink.backend.aggregation.events.field_type_detectors;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors.DoubleFieldTypeDetectionStrategy;

public class DoubleFieldTypeDetectionStrategyTest {

    private final DoubleFieldTypeDetectionStrategy sut = new DoubleFieldTypeDetectionStrategy();

    @Test
    public void shouldDetectIfFieldIsADoubleField1() {
        assertTrue(sut.isTypeMatched(Collections.emptyList(), "1.2", JsonNodeType.NUMBER));
    }

    @Test
    public void shouldDetectIfFieldIsADoubleField2() {
        assertTrue(sut.isTypeMatched(Collections.emptyList(), "1,2", JsonNodeType.NUMBER));
    }

    @Test
    public void shouldDetectIfFieldIsADoubleField3() {
        assertTrue(sut.isTypeMatched(Collections.emptyList(), ",2", JsonNodeType.NUMBER));
    }

    @Test
    public void shouldDetectIfFieldIsNotADoubleField1() {
        assertFalse(sut.isTypeMatched(Collections.emptyList(), "abc12", JsonNodeType.STRING));
    }
}
