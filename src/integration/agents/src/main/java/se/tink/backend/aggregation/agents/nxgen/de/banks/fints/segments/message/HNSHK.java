package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

public class HNSHK extends FinTsSegment{

    public HNSHK(int segmentNumber, int profileVersion, int securityReference, String securityFunction,
            String systemId, String blz, String username) {
        super(segmentNumber, false);

        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String time = now.format(DateTimeFormatter.ofPattern("HHmmss"));

        addDataGroup("PIN", String.valueOf(profileVersion));
        addDataGroup(securityFunction);
        addDataGroup(securityReference);
        addDataGroup(FinTsConstants.SegData.SECURITY_BOUNDARY);
        addDataGroup(FinTsConstants.SegData.SECURITY_SUPPLIER_ROLE);
        addDataGroup("1", "", systemId);
        addDataGroup("1");
        addDataGroup("1", date, time);
        addDataGroup("1", "999", "1"); // Negotiate hash algorithm
        addDataGroup("6", "10", "16"); // RSA mode
        addDataGroup(FinTsConstants.SegData.COUNTRY_CODE, blz, username, "S", "0", "0");
    }

    @Override
    public int getVersion() {
        return 4;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HNSHK;
    }
}
