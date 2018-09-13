package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OwnRecipientEntity extends RecipientEntity {
    public OwnRecipientEntity() {
        super();
    }

    public OwnRecipientEntity(AccountEntity accountEntity) {
        this.setRecipientId(accountEntity.getAccountId());
        this.setAccountNumber(accountEntity.getAccountNumber());
    }

    @Override
    public boolean isOwnAccount() {
        return true;
    }
}
