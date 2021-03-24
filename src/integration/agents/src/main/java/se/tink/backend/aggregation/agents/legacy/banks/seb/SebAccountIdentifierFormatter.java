package se.tink.backend.aggregation.agents.banks.seb;

import com.google.common.base.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.banks.seb.model.ExternalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;
import se.tink.libraries.account.identifiers.se.swedbank.SwedbankClearingNumberUtils;
import se.tink.libraries.social.security.SocialSecurityNumber;

public class SebAccountIdentifierFormatter implements AccountIdentifierFormatter {
    private static final String NORDEA_BANK_PREFIX = "NB";
    private static final String NORDEA_PERSONKONTO_CLEARINGNUMBER = "3300";

    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();

    @Override
    public String apply(AccountIdentifier identifier) {
        if (identifier.is(AccountIdentifierType.SE)) {
            return applySwedish(identifier.to(SwedishIdentifier.class));
        }

        return identifier.getIdentifier(DEFAULT_FORMATTER);
    }

    private String applySwedish(SwedishIdentifier swedishIdentifier) {
        switch (swedishIdentifier.getBank()) {
            case NORDEA_PERSONKONTO:
                return swedishIdentifier.getAccountNumber(); // Remove clearing number.
            case SWEDBANK:
                if (swedishIdentifier.getClearingNumber().startsWith("8")) {
                    return SwedbankClearingNumberUtils.padWithZerosBetweenClearingAndAccountNumber(
                            swedishIdentifier);
                }
                break;
            default:
                break;
        }

        return swedishIdentifier.getIdentifier(DEFAULT_FORMATTER);
    }

    /** Parses the users' own accounts, that should by definition be interpreted as SEB */
    public Optional<SwedishIdentifier> parseInternalIdentifier(String accountNumber) {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(accountNumber);

        if (!swedishIdentifier.isValid() || !(swedishIdentifier.getBank() == Bank.SEB)) {
            return Optional.empty();
        }

        return Optional.of(swedishIdentifier);
    }

    /** Parses SE_BG, SE_PG and SE identifiers from SEB account entities */
    public Optional<? extends AccountIdentifier> parseExternalIdentifier(
            ExternalAccount externalAccount) {
        if (externalAccount.isBankGiro()) {
            return parseBG(externalAccount.BankgiroNumber);
        } else if (externalAccount.isPostGiro()) {
            return parsePG(externalAccount.PostgiroNumber);
        } else {
            return parseSwedishIdentifier(
                    externalAccount.DestinationNumber, externalAccount.BankPrefix);
        }
    }

    private Optional<? extends AccountIdentifier> parsePG(String pgNumber) {
        PlusGiroIdentifier plusGiroIdentifier = new PlusGiroIdentifier(pgNumber);

        if (!plusGiroIdentifier.isValid()) {
            return Optional.empty();
        }

        return Optional.of(plusGiroIdentifier);
    }

    private Optional<? extends AccountIdentifier> parseBG(String bgNumber) {
        BankGiroIdentifier bankGiroIdentifier = new BankGiroIdentifier(bgNumber);

        if (!bankGiroIdentifier.isValid()) {
            return Optional.empty();
        }

        return Optional.of(bankGiroIdentifier);
    }

    /**
     * Parses any SwedishIdentifier, and regards e.g. that Nordea identifiers can be Swedish SSNs
     * that doesn't contain clearingnumbers
     */
    Optional<SwedishIdentifier> parseSwedishIdentifier(
            String destinationNumber, String bankPrefix) {
        if (isNordeaAccount(bankPrefix)) {
            return parseNordeaAccount(destinationNumber);
        } else {
            return parseNonNordeaAccount(destinationNumber);
        }
    }

    private boolean isNordeaAccount(String bankPrefix) {
        return Objects.equal(bankPrefix, NORDEA_BANK_PREFIX);
    }

    private Optional<SwedishIdentifier> parseNordeaAccount(String destinationNumber) {
        SocialSecurityNumber.Sweden swedishSocialSecurityNumber =
                new SocialSecurityNumber.Sweden(destinationNumber);

        SwedishIdentifier swedishIdentifier;
        if (swedishSocialSecurityNumber.isValid()) {
            swedishIdentifier =
                    new SwedishIdentifier(NORDEA_PERSONKONTO_CLEARINGNUMBER + destinationNumber);
        } else {
            swedishIdentifier = new SwedishIdentifier(destinationNumber);
        }

        if (!isValidNordeaIdentifier(swedishIdentifier)) {
            return Optional.empty();
        }

        return Optional.of(swedishIdentifier);
    }

    private boolean isValidNordeaIdentifier(SwedishIdentifier swedishIdentifier) {
        if (!swedishIdentifier.isValid()) {
            return false;
        }

        Bank bank = swedishIdentifier.getBank();
        return Objects.equal(bank, Bank.NORDEA) || Objects.equal(bank, Bank.NORDEA_PERSONKONTO);
    }

    private Optional<SwedishIdentifier> parseNonNordeaAccount(String destinationNumber) {
        SwedishIdentifier parsedIdentifier = new SwedishIdentifier(destinationNumber);

        if (!parsedIdentifier.isValid()) {
            return Optional.empty();
        }

        return Optional.of(parsedIdentifier);
    }
}
