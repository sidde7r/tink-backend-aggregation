package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;

public class HandelsbankenAccountEntity implements GeneralAccountEntity {

    private final AccountIdentifier identifier;
    private final String bankName;
    private final String name;

    public HandelsbankenAccountEntity(AccountIdentifier identifier, String bankName, String name) {
        this.identifier = identifier;
        this.bankName = bankName;
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
