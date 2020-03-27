package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIPINS;

public class TanByOperationLookupTest {

    @Test
    public void shouldReturnProperInformationAboutRegisteredOperations() {
        // given
        HIPINS hipins = getHIPINS();
        TanByOperationLookup lookup = new TanByOperationLookup(hipins);

        for (Pair<SegmentType, Boolean> operationInfo : getExpectedOperations()) {
            SegmentType segmentType = operationInfo.getLeft();
            Boolean requiresTAN = operationInfo.getRight();
            // when
            boolean result = lookup.doesOperationRequireTAN(segmentType);

            // then
            assertThat(result).isEqualTo(requiresTAN);
        }
    }

    @Test
    public void shouldAlwaysReturnTrueForOperationsThatAreNotSetUpInLookup() {
        // given
        HIPINS hipins = getHIPINS();
        TanByOperationLookup lookup = new TanByOperationLookup(hipins);
        List<SegmentType> unregisteredOperations =
                Arrays.asList(SegmentType.HKWOA, SegmentType.HKWSD);

        for (SegmentType unregisteredOperation : unregisteredOperations) {
            // when
            boolean result = lookup.doesOperationRequireTAN(unregisteredOperation);

            // then
            assertThat(result).isTrue();
        }
    }

    private HIPINS getHIPINS() {
        return new HIPINS(RawSegmentComposer.compose(getHIPINSArray()));
    }

    private String[][] getHIPINSArray() {
        return new String[][] {
            new String[] {"HIPINS", "7", "1", "4"},
            new String[] {"1"},
            new String[] {"1"},
            new String[] {"0"},
            new String[] {
                "5",
                "50",
                "6",
                "Kunden-Nr aus dem TAN-Brief",
                "Customer Field",
                "HKCSE",
                "J",
                "HKCSL",
                "J",
                "HKPAE",
                "J",
                "HKTAB",
                "N",
                "HKEKA",
                "N",
                "DKPAE",
                "J"
            }
        };
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
