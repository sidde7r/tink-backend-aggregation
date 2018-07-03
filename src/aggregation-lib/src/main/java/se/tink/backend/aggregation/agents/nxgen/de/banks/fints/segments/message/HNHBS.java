package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

public class HNHBS extends FinTsSegment {

    public HNHBS(int segmentNumber, int messageNumber) {
        super(segmentNumber);

        addDataGroup(String.valueOf(messageNumber));
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HNHBS;
    }
}
