package se.tink.backend.aggregation.agents.banks.se.alandsbanken;

import java.util.List;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.AbstractAlandsBankenConfig;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class AlandsBankenConfig extends AbstractAlandsBankenConfig {

    @Override
    public List<AccountIdentifier> getIdentifiers(String bic, String iban, String bban) {
        List<AccountIdentifier> identifiers = super.getIdentifiers(bic, iban);
        identifiers.add(new SwedishIdentifier(bban));

        return identifiers;
    }
}
