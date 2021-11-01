package se.tink.backend.aggregation.events.field_type_detectors;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo.FieldPathPart;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors.BbanFieldTypeDetectionStrategy;

public class BbanFieldTypeDetectionStrategyTest {

    private final BbanFieldTypeDetectionStrategy sut = new BbanFieldTypeDetectionStrategy();

    @Test
    public void shouldDetectIfFieldIsABbanField() {
        assertTrue(
                sut.isTypeMatched(
                        Arrays.asList(
                                new FieldPathPart("account", true),
                                new FieldPathPart("bban", false)),
                        "611824429039",
                        JsonNodeType.STRING));
    }

    @Test
    public void shouldNotDetectIfFieldHasNonBbanValue1() {
        assertFalse(
                sut.isTypeMatched(
                        Arrays.asList(
                                new FieldPathPart("account", true),
                                new FieldPathPart("bban", false)),
                        "dummy-value",
                        JsonNodeType.STRING));
    }

    @Test
    public void shouldNotDetectIfFieldHasNonBbanValue2() {
        assertFalse(
                sut.isTypeMatched(
                        Collections.singletonList(new FieldPathPart("dummy-field", false)),
                        "611824429039",
                        JsonNodeType.STRING));
    }
}
