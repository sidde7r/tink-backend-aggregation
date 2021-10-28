package se.tink.backend.aggregation.events.field_type_detectors;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors.SortCodeFieldTypeDetectionStrategy;

public class SortCodeFieldTypeDetectionStrategyTest {

    private final SortCodeFieldTypeDetectionStrategy sut = new SortCodeFieldTypeDetectionStrategy();

    @Test
    public void shouldDetectIfFieldIsASortCodeField1() {
        assertTrue(
                sut.isTypeMatched(Collections.emptyList(), "31245678901234", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsNotASortCodeField2() {
        assertFalse(
                sut.isTypeMatched(
                        Collections.emptyList(), "312456789012345678", JsonNodeType.STRING));
    }
}
