package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

public class OwnRecipientEntity extends RecipientEntity {
    public OwnRecipientEntity() {
        super();
    }

    public OwnRecipientEntity(AccountEntity accountEntity) {
        this.setRecipientId(accountEntity.accountId);
        this.setAccountNumber(accountEntity.accountNumber);
    }

    @Override
    public boolean isOwnAccount() {
        return true;
    }
}
