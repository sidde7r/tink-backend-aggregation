package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class BalanceBankConfig extends BaseResponsePart {
    BalanceBankConfig(RawSegment rawSegment) {
        super(rawSegment);
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Arrays.asList(3, 4, 5, 6, 7);
    }
}
