package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

// HNVSK (Verschlüsslungskopf)
// Section B.5.3
public class HNVSK extends FinTsSegment {

    public HNVSK(int segmentNumber, int profileVersion, String systemId, String blz, String username) {
        super(segmentNumber, false);

        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String time = now.format(DateTimeFormatter.ofPattern("HHmmss"));

        addDataGroup("PIN", String.valueOf(profileVersion));
        addDataGroup("998"); // ??
        addDataGroup(FinTsConstants.SegData.SECURITY_SUPPLIER_ROLE);
        addDataGroup("1", "", systemId);
        addDataGroup("1", date, time);
        addDataGroup("2", "2", "13", "@8@00000000", "5", "1"); // Crypto algorithm
        addDataGroup(FinTsConstants.SegData.COUNTRY_CODE, blz, username, "S", "0", "0");
        addDataGroup(FinTsConstants.SegData.COMPRESSION_NONE);
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HNVSK;
    }
}