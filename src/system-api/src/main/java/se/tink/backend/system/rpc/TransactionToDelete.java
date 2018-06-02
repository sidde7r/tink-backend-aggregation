package se.tink.backend.system.rpc;

public class TransactionToDelete {
    private String externalId;
    private String accountId;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public static TransactionToDelete create(String externalId, String accountId) {
        TransactionToDelete toDelete = new TransactionToDelete();
        toDelete.setExternalId(externalId);
        toDelete.setAccountId(accountId);
        return toDelete;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
