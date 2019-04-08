package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transferdestination;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.libraries.account.AccountIdentifier;

public class TransferAccountEntity implements GeneralAccountEntity {

    private final AccountIdentifier identifier;
    private final String bankName;
    private final String name;

    public TransferAccountEntity(AccountIdentifier identifier, String name) {
        this.identifier = identifier;
        this.bankName = IngConstants.Transfers.BANK_NAME;
        this.name = name;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return identifier;
    }

    @Override
    public String generalGetBank() {
        return bankName;
    }

    @Override
    public String generalGetName() {
        return name;
    }
}
