package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DanskeDkConstants {

    public static final Pattern EXTRACT_ACCOUNT_NO_FROM_IBAN_PATTERN =
            Pattern.compile("DK\\d{2}3000(\\d+)");
    public static final int ACCOUNT_NO_MIN_LENGTH = 10;
    public static final int BRANCH_CODE_LENGTH = 4;
    public static final int CHARS_TO_SUBSTRING_FROM_IBAN = 8;
}
