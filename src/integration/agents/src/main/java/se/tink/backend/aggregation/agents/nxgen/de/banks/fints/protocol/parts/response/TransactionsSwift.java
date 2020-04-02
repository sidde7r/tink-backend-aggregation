package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Getter
public class TransactionsSwift extends BaseResponsePart {

    private String booked;
    private String notBooked;

    TransactionsSwift(RawSegment rawSegment) {
        super(rawSegment);
        booked = rawSegment.getGroup(1).getString(0);
        notBooked = rawSegment.getGroup(2).getString(0);
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Arrays.asList(4, 5, 6, 7);
    }
}
