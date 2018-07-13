package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class FromAccountEntity extends AbstractAccountEntity implements GeneralAccountEntity {
    private String currencyCode;
    private String amount;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getAmount() {
        return amount;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(fullyFormattedNumber);
    }

    @Override
    public String generalGetBank() {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(fullyFormattedNumber);
        if (!swedishIdentifier.isValid()) {
            return null;
        }

        return swedishIdentifier.getBankName();
    }

    @Override
    public String generalGetName() {
        return name;
    }
}
