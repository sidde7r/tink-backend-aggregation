package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.auth;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.SegData.PRODUCT_VERSION;

public class HKVVB extends FinTsSegment {

    public HKVVB(int segmentNumber, String regNumber) {
        super(segmentNumber);
        addDataGroup(FinTsConstants.SegData.DEFAULT_BPD_VERSION);
        addDataGroup(FinTsConstants.SegData.DEFAULT_UPD_VERSION);
        addDataGroup(FinTsConstants.SegData.LANGUAGE_DE);
        addDataGroup(regNumber);
        addDataGroup(PRODUCT_VERSION);
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HKVVB;
    }
}
