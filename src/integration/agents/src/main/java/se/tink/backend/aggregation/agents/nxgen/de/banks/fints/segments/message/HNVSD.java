package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

// HNVSD (Verschlüsselte Daten)
// Section B.5.4
public class HNVSD extends FinTsSegment {

    private String encryptedData = "";

    public HNVSD(int segmentNumber) {
        super(segmentNumber, false);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HNVSD;
    }

    public void appendEncryptedSegment(FinTsSegment segment) {
        encryptedData += segment.toString();
    }

    @Override
    public String toString() {
        clearData();
        addDataGroup(String.format("@%d@%s", encryptedData.length(), encryptedData));
        return super.toString();
    }
}