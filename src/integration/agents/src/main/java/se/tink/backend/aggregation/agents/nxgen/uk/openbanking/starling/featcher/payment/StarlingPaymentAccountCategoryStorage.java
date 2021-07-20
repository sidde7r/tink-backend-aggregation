package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.payment;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class StarlingPaymentAccountCategoryStorage {

    private static final String CACHED_ACCOUNTS = "cached_accounts";
    private static final String CACHED_ACCOUNT_IDENTIFIERS = "cached_identifiers";
    private final PersistentStorage persistentStorage;

    public StarlingPaymentAccountCategoryStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public String getAccountUuid(String identifier) {
        return persistentStorage.get(identifier);
    }

    public String getCategoryUuid(String accountUuid) {
        return persistentStorage.get(accountUuid);
    }

    public void saveAccountIdentifierWithAccountUuid(
            AccountIdentifiersResponse accountIdentifiersResponse, String accountUuid) {
        persistentStorage.put(
                accountIdentifiersResponse.getSortCodeAccountNumber().getIdentifier(), accountUuid);
        persistentStorage.put(CACHED_ACCOUNT_IDENTIFIERS, true);
    }

    public void saveAccountUuidWithCategoryUuid(List<AccountEntity> accountEntities) {
        for (AccountEntity accountEntity : accountEntities) {
            persistentStorage.put(
                    accountEntity.getAccountUid(), accountEntity.getDefaultCategory());
        }
        persistentStorage.put(CACHED_ACCOUNTS, true);
    }

    public boolean hasAccountsCached() {
        return persistentStorage.containsKey(CACHED_ACCOUNTS)
                && persistentStorage.containsKey(CACHED_ACCOUNT_IDENTIFIERS);
    }
}
