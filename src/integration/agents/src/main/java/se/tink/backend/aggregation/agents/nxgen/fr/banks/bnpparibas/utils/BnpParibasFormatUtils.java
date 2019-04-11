package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.utils;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class BnpParibasFormatUtils {
    public static final ThreadSafeDateFormat TRANSACTION_DATE_FORMATTER =
            new ThreadSafeDateFormat("ddMMyyyy");

    public static final Pattern TRANSACTION_DESCRIPTION_PATTERN =
            Pattern.compile(BnpParibasConstants.TransactionDescriptionFormatting.REGEX);
}
