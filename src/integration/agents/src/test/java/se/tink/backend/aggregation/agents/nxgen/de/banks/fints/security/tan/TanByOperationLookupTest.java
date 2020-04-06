package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIPINS;

@RunWith(JUnitParamsRunner.class)
public class TanByOperationLookupTest {

    private Object[] expectedOperationsAndTanRequirement() {
        return new Object[] {
            new Object[] {SegmentType.HKCSE, true},
            new Object[] {SegmentType.HKCSL, true},
            new Object[] {SegmentType.HKPAE, true},
            new Object[] {SegmentType.HKTAB, false},
            new Object[] {SegmentType.HKEKA, false},
            new Object[] {SegmentType.DKPAE, true}
        };
    }

    @Test
    @Parameters(method = "expectedOperationsAndTanRequirement")
    public void shouldReturnProperInformationAboutRegisteredOperations(
            SegmentType operation, boolean expectedTanRequirement) {
        // given
        TanByOperationLookup lookup = new TanByOperationLookup(getHIPINS());

        // when
        boolean result = lookup.doesOperationRequireTAN(operation);

        // then
        assertThat(result).isEqualTo(expectedTanRequirement);
    }

    private Object[] operationsNotSetUpInLookup() {
        return new Object[] {SegmentType.HKWOA, SegmentType.HKWSD, SegmentType.DKALE};
    }

    @Test
    @Parameters(method = "operationsNotSetUpInLookup")
    public void shouldAlwaysReturnTrueForOperationsThatAreNotSetUpInLookup(
            SegmentType operationNotInLookup) {
        // given
        TanByOperationLookup lookup = new TanByOperationLookup(getHIPINS());

        // when
        boolean result = lookup.doesOperationRequireTAN(operationNotInLookup);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Parameters(method = "operationsNotSetUpInLookup")
    public void shouldReportOperationAsNotSupportedIfNotFoundInLookup(
            SegmentType operationNotInLookup) {
        // given
        TanByOperationLookup lookup = new TanByOperationLookup(getHIPINS());

        // when
        boolean result = lookup.isOperationSupported(operationNotInLookup);

        // then
        assertThat(result).isFalse();
    }

    private HIPINS getHIPINS() {
        return new HIPINS().setOperations(getOperationsList());
    }

    private List<Pair<String, Boolean>> getOperationsList() {
        return Arrays.asList(
                Pair.of("HKCSE", true),
                Pair.of("HKCSL", true),
                Pair.of("HKPAE", true),
                Pair.of("HKTAB", false),
                Pair.of("HKEKA", false),
                Pair.of("DKPAE", true));
    }

    private List<Pair<SegmentType, Boolean>> getExpectedOperations() {
        return Arrays.asList(
                Pair.of(SegmentType.HKCSE, true),
                Pair.of(SegmentType.HKCSL, true),
                Pair.of(SegmentType.HKPAE, true),
                Pair.of(SegmentType.HKTAB, false),
                Pair.of(SegmentType.HKEKA, false),
                Pair.of(SegmentType.DKPAE, true));
    }
}
