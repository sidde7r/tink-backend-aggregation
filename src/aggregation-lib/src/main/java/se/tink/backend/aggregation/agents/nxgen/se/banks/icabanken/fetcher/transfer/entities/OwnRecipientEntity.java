package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities.OwnAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OwnRecipientEntity extends RecipientEntity {
    public OwnRecipientEntity() {
        super();
    }

    public OwnRecipientEntity(OwnAccountsEntity ownAccountsEntity) {
        this.setRecipientId(ownAccountsEntity.getAccountId());
        this.setAccountNumber(ownAccountsEntity.getAccountNumber());
    }

    @Override
    public boolean isOwnAccount() {
        return true;
    }
}
