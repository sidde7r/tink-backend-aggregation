package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.saldo;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

//    HKSAL (Konto Saldo anfordern)
//            Section C.2.1.2
public class HKSAL extends FinTsSegment {

    private int version;

    public HKSAL(int segmentNumber, int version, String account) {
        super(segmentNumber, false);

        this.version = version;

        addDataGroup(account);
        addDataGroup("N");
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HKSAL;
    }
}
