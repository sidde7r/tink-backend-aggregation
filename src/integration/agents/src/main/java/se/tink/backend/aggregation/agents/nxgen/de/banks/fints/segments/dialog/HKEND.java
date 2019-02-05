package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.dialog;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

public class HKEND extends FinTsSegment {

    public HKEND(int segmentNumber, String dialogId) {
        super(segmentNumber);
        addDataGroup(dialogId);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HKEND;
    }
}
