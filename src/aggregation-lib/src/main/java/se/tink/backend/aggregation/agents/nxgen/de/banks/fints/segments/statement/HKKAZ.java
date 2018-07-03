package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.statement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsEscape;

public class HKKAZ extends FinTsSegment {

    private int version;

    public HKKAZ(int segmentNumber, int version, String account, LocalDateTime dateStart, LocalDateTime dateEnd,
            String touchDown) {
        super(segmentNumber, false);

        this.version = version;
        addDataGroup(account);
        addDataGroup("N");
        addDataGroup(dateStart.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        addDataGroup(dateEnd.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        addDataGroup("");
        addDataGroup(touchDown == null ? "" : FinTsEscape.escapeDataElement(touchDown));
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HKKAZ;
    }
}
