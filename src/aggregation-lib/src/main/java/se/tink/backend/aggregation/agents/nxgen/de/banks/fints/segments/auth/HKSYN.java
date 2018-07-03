package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.auth;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

// HKSYN (Synchronisation)
// Section C.8.1.2
public class HKSYN extends FinTsSegment {

    private final static int SYNC_MODE_NEW_CUSTOMER_ID = 0;
    private final static int SYNC_MODE_LAST_MSG_NUMBER = 1;
    private final static int SYNC_MODE_SIGNATURE_ID = 2;

    public HKSYN(int segmentNumber, int mode) {
        super(segmentNumber);
        addDataGroup(mode); // mode
    }

    public HKSYN(int segmentNumber) {
        this(segmentNumber, SYNC_MODE_NEW_CUSTOMER_ID);
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HKSYN;
    }
}