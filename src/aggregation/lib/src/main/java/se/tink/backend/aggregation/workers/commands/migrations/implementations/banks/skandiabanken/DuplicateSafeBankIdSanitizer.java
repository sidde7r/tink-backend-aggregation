package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.skandiabanken;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Migration will append `-duplicate-n` behind duplicated accounts. This suffix needs to be
 * preserved and must not interfere with sanitization.
 *
 * <p>This class will split the bankId into the bankId which should be sanitized, and the suffix
 * which should be re-appended after sanitization.
 */
public class DuplicateSafeBankIdSanitizer {

    private static final String DUPLICATE_PATTERN_STRING = "(.*)(-duplicate(-(\\d)+){0,1})";

    private final String bankId;
    private final String duplicateSuffix;

    private DuplicateSafeBankIdSanitizer(final String wholeBankId) {

        Matcher m = Pattern.compile(DUPLICATE_PATTERN_STRING).matcher(wholeBankId);

        if (m.find()) {
            bankId = m.group(1);
            duplicateSuffix = m.group(2);
        } else {

            bankId = wholeBankId;
            duplicateSuffix = "";
        }
    }

    /** @param wholeBankId The unsanitized complete bankId, including duplication suffix. */
    public static DuplicateSafeBankIdSanitizer from(final String wholeBankId) {
        return new DuplicateSafeBankIdSanitizer(wholeBankId);
    }

    /**
     * Removes all non-digits from the the bankId, preserving duplication suffix.
     *
     * @return Sanitized bankId with duplication suffix if one existed.
     */
    public String getSanitizeInvestmentBankId() {
        return bankId.replaceAll("[^\\d]", "") + duplicateSuffix;
    }

    /**
     * Transforms {bankId}-{bankId} to format {bankId}. Preserving duplication suffix.
     *
     * @return Sanitized bankId with duplication suffix if one existed.
     */
    public String getSanitizeTransactionalAccountBankId() {
        return bankId.replaceAll("[^\\d]\\w+\\Z", "") + duplicateSuffix;
    }
}
