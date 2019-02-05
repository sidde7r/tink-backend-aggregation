package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

public class HNHBK extends FinTsSegment {

    private final static int HEADER_LENGTH = 29;
    private final static String FINTS3_VERSION = "300"; // version 3.0

    public HNHBK(int messageLength, String dialogId, int messageNumber) {
        super(1, false);// always the first segment

        Preconditions.checkArgument(messageNumber > 0, "Invalid message number, must be > 0.");

        String msgLength = String.valueOf(messageLength);
        if (msgLength.length() != 12) {
            msgLength = String.format("%012d", messageLength +
                    HEADER_LENGTH +
                    dialogId.length() +
                    String.valueOf(messageNumber).length());
        }

        addDataGroup(msgLength);
        addDataGroup(FINTS3_VERSION);
        addDataGroup(dialogId);
        addDataGroup(messageNumber);
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HNHBK;
    }

}
