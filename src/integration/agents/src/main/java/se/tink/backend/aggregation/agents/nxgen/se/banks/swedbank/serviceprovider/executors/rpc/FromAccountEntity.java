package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@Getter
public class FromAccountEntity extends AbstractAccountEntity implements GeneralAccountEntity {
    private String currencyCode;
    private String amount;

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
