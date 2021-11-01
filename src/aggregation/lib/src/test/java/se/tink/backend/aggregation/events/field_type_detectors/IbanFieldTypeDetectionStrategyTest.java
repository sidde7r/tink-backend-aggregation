package se.tink.backend.aggregation.events.field_type_detectors;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors.IbanFieldTypeDetectionStrategy;

public class IbanFieldTypeDetectionStrategyTest {

    private final IbanFieldTypeDetectionStrategy sut = new IbanFieldTypeDetectionStrategy();

    @Test
    public void shouldDetectIfFieldIsAnIbanField1() {
        assertTrue(
                sut.isTypeMatched(
                        Collections.emptyList(), "SE4191500000091501234567", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsAnIbanField2() {
        assertTrue(
                sut.isTypeMatched(
                        Collections.emptyList(), "ES7420387294088391363592", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsNotAnIbanField1() {
        assertFalse(
                sut.isTypeMatched(
                        Collections.emptyList(), "ES0000000000000000000000", JsonNodeType.STRING));
    }

    @Test
    public void shouldDetectIfFieldIsNotAnIbanField2() {
        assertFalse(sut.isTypeMatched(Collections.emptyList(), "dummy-value", JsonNodeType.STRING));
    }
}
