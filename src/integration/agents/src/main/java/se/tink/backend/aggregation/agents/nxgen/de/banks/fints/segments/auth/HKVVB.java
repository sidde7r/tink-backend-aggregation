package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.auth;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.SegData.PRODUCT_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.TINK_REGISTERED_PRODUCT_ID;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

public class HKVVB extends FinTsSegment {

    public HKVVB(int segmentNumber) {
        super(segmentNumber);
        addDataGroup(FinTsConstants.SegData.DEFAULT_BPD_VERSION);
        addDataGroup(FinTsConstants.SegData.DEFAULT_UPD_VERSION);
        addDataGroup(FinTsConstants.SegData.LANGUAGE_DE);
        addDataGroup(TINK_REGISTERED_PRODUCT_ID);
        addDataGroup(PRODUCT_VERSION);
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HKVVB.name();
    }
}
