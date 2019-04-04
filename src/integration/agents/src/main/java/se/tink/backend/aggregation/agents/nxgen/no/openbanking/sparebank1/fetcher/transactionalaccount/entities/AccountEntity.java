package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private AccountNumberEntity accountNumber;

    private BalanceEntity availableBalance;

    private BalanceEntity balance;

    private String description;

    private String iban;

    private String id;

    private String name;

    private OwnerEntity owner;

    private String product;

    private String type;

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(accountNumber.getValue())
                .setBalance(balance.toAmount())
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addHolderName(owner.getName())
                .setAlias(name)
                .setApiIdentifier(id)
                .putInTemporaryStorage(SpareBank1Constants.StorageKeys.ACCOUNT_ID, id)
                .build();
    }

    public boolean isCheckingAccount() {
        return SpareBank1Constants.ACCOUNT_TYPE_MAPPER
                .translate(type)
                .get()
                .equals(AccountTypes.CHECKING);
    }
}
