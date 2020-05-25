package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities;

import java.util.regex.Pattern;

public class SwedbankNoteToRecipientUtils {
    private static final String LETTERS_AND_NUMBERS = "^[0-9a-zA-ZäöåÄÖÅ\\s]+$";
    private static Pattern LETTERS_AND_NUMBERS_PATTERN = Pattern.compile(LETTERS_AND_NUMBERS);

    public static boolean isValidSwedbankNoteToRecipient(String destinationMessage) {
        if (destinationMessage == null) {
            return true;
        }
        return LETTERS_AND_NUMBERS_PATTERN.matcher(destinationMessage).find();
    }
}
