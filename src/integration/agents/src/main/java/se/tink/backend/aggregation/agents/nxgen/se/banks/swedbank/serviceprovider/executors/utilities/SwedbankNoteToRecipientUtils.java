package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities;

import java.util.regex.Pattern;

public class SwedbankNoteToRecipientUtils {
    private static final String ILLEGAL_CHARS = "[;><%&±]";
    private static Pattern ILLEGAL_CHARS_PATTERN = Pattern.compile(ILLEGAL_CHARS);

    private SwedbankNoteToRecipientUtils() {}

    public static boolean isValidSwedbankNoteToRecipient(String remittanceInformationValue) {
        if (remittanceInformationValue == null) {
            return true;
        }
        return !ILLEGAL_CHARS_PATTERN.matcher(remittanceInformationValue).find();
    }
}
