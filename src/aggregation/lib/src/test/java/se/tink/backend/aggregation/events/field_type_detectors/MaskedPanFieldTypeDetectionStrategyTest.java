package se.tink.backend.aggregation.events.field_type_detectors;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors.MaskedPanFieldTypeDetectionStrategy;

public class MaskedPanFieldTypeDetectionStrategyTest {

    private final MaskedPanFieldTypeDetectionStrategy sut =
            new MaskedPanFieldTypeDetectionStrategy();

    @Test
    public void shouldDetectIfFieldIsAMaskedPanField1() {
        assertTrue(
                sut.isTypeMatched(
                        Collections.emptyList(), "5269 **** **** 3239", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsAMaskedPanField2() {
        assertTrue(
                sut.isTypeMatched(
                        Collections.emptyList(), "5269********3239", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsAMaskedPanField3() {
        assertTrue(
                sut.isTypeMatched(
                        Collections.emptyList(), "************3239", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsNotAMaskedPanField1() {
        assertFalse(
                sut.isTypeMatched(
                        Collections.emptyList(), "5269 1111 1111 3239", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsNotAMaskedPanField2() {
        assertFalse(sut.isTypeMatched(Collections.emptyList(), "dummy-value", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsNotAMaskedPanField3() {
        assertFalse(sut.isTypeMatched(Collections.emptyList(), "2021-05-18", JsonNodeType.STRING));
    }
}
