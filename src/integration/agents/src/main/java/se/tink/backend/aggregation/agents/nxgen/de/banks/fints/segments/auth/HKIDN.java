package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.auth;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

// HKIDN (Identifikation)
// Section C.3.1.2
public class HKIDN extends FinTsSegment {

    public HKIDN(int segmentNumber, String blz, String username, String systemId) {
        super(segmentNumber, false);

        addDataGroup(FinTsConstants.SegData.COUNTRY_CODE, blz);
        addDataGroup(username);
        addDataGroup(systemId);
        addDataGroup(FinTsConstants.SegData.CUSTOMER_ID);
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HKIDN;
    }
}
