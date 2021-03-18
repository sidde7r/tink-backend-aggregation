package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;

public class NordeaAccountIdentifierFormatter implements AccountIdentifierFormatter {
    private static final Logger log =
            LoggerFactory.getLogger(NordeaAccountIdentifierFormatter.class);

    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();

    @Override
    public String apply(AccountIdentifier identifier) {
        if (identifier.is(AccountIdentifierType.SE)) {
            SwedishIdentifier swedishIdentifier = getSwedishIdentifier(identifier);
            if (swedishIdentifier.getBank() == Bank.NORDEA_PERSONKONTO) {
                return applyNordeaPersonkonto(swedishIdentifier);
            }
        }

        return identifier.getIdentifier(DEFAULT_FORMATTER);
    }

    private SwedishIdentifier getSwedishIdentifier(AccountIdentifier identifier) {
        SwedishIdentifier swedishIdentifier = identifier.to(SwedishIdentifier.class);
        if (swedishIdentifier == null) {
            String errorMessage =
                    "SwedishIdentifier is null. Supplied account identifier: "
                            + identifier.toString();
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        return swedishIdentifier;
    }

    /**
     * Nordea Personkonto (account based on SSN number) should not include clearing number when
     * doing transfers.
     */
    private String applyNordeaPersonkonto(SwedishIdentifier swedishIdentifier) {
        return swedishIdentifier.getAccountNumber();
    }
}
