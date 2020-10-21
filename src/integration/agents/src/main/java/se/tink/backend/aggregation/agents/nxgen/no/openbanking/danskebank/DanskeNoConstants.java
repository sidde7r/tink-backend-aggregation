package se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DanskeNoConstants {

    public static final Pattern EXTRACT_BBAN_FROM_IBAN_PATTERN = Pattern.compile("NO\\d{2}(\\d+)");
    public static final int ACCOUNT_NO_MIN_LENGTH = 11;
    public static final int CHARS_TO_SUBSTRING_FROM_IBAN = 4;
}
