package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class TransactionsSwiftBankConfig extends BaseResponsePart {

    TransactionsSwiftBankConfig(RawSegment rawSegment) {
        super(rawSegment);
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Arrays.asList(4, 5, 6, 7);
    }
}
