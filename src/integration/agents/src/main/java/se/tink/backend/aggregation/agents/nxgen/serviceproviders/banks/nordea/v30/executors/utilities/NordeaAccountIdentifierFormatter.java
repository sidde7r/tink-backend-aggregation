package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.utilities;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.swedbank.SwedbankClearingNumberUtils;

public class NordeaAccountIdentifierFormatter implements AccountIdentifierFormatter {
    private static final Logger log =
            LoggerFactory.getLogger(NordeaAccountIdentifierFormatter.class);

    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();

    @Override
    public String apply(AccountIdentifier identifier) {
        if (identifier.is(AccountIdentifierType.SE)) {
            SwedishIdentifier swedishIdentifier = identifier.to(SwedishIdentifier.class);

            try {
                switch (swedishIdentifier.getBank()) {
                    case SWEDBANK:
                        return applySwedbank(swedishIdentifier);
                    case HANDELSBANKEN:
                        return applyHandelsbanken(swedishIdentifier);
                    case NORDEA_PERSONKONTO:
                        return applyNordeaPersonkonto(swedishIdentifier);
                    default:
                        break;
                }
            } catch (NullPointerException e) {
                log.error(
                        "Nordea NPE when formatting account identifier. Supplied account identifier: {}",
                        identifier.toString());

                throw e;
            }
        }

        return identifier.getIdentifier(DEFAULT_FORMATTER);
    }

    /** Prepends account number with zeros to 15 digits for 8xxxx Swedbank clearings. */
    private String applySwedbank(SwedishIdentifier identifier) {
        if (identifier.getClearingNumber().startsWith("8")) {
            return SwedbankClearingNumberUtils.padWithZerosBetweenClearingAndAccountNumber(
                    identifier);
        } else {
            return identifier.getIdentifier(DEFAULT_FORMATTER);
        }
    }

    /**
     * Nordea has special logic for Handelsbanken. An account can have a clearing number on either 4
     * or 5 digits depending on if the recipient was added on their web site or in their mobile app
     * and with or without a clearing number. This is the logic. Web Site - Added with clearing
     * number, Input: 6111 111 111 111 => Result: 6111 111 111 111 - Added without clearing number,
     * Input: 111 111 111 => Result: 6000 111 111 111 - Clearing Number 6000 is automatically added
     *
     * <p>Mobile App - With clearing number, Input: 6111 111 111 111 => Result: 61110 111 111 111 -
     * Without clearing number, Input: 111 111 111 => Result: 60000 111 111 111 - Clearing Number
     * 60000 is automatically added
     *
     * <p>Account numbers that are below 9 digits are padded with zeros
     *
     * <p>We pad every account number with zeros up to 10 digits so we use the Mobile App way of
     * storing information.
     *
     * <p>If account number < 10 digits, prepend with zeros
     */
    private String applyHandelsbanken(SwedishIdentifier identifier) {
        String accountNumber = Strings.padStart(identifier.getAccountNumber(), 10, '0');
        return identifier.getClearingNumber() + accountNumber;
    }

    /**
     * Nordea Personkonto (account based on SSN number) should not include clearing number when
     * doing transfers.
     */
    private String applyNordeaPersonkonto(SwedishIdentifier swedishIdentifier) {
        return swedishIdentifier.getAccountNumber();
    }
}
