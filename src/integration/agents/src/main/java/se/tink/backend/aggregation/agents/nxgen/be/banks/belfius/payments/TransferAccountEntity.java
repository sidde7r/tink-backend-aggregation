package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;

public class TransferAccountEntity implements GeneralAccountEntity {

    private String bankName;
    private String name;
    private AccountIdentifier accountIdentifier;

    public TransferAccountEntity(
            AccountIdentifier accountIdentifier, String bankName, String name) {
        this.name = name;
        this.bankName = bankName;
        this.accountIdentifier = accountIdentifier;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return accountIdentifier;
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
