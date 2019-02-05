package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsEscape;

// HNSHA (Signaturabschluss)
// Section B.5.2
public class HNSHA extends FinTsSegment {

    public HNSHA(int segmentNumber, int securityReference, String password) {
        super(segmentNumber);

        addDataGroup(securityReference);
        addDataGroup("");
        addDataGroup(FinTsEscape.escapeDataElement(password));
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HNSHA;
    }
}