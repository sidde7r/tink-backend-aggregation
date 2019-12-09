package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String id;

    @JsonProperty("bank_id")
    private String bankId;

    private String label;

    private String number;

    private List<OwnerEntity> owners;

    private BalanceEntity balance;

    private String type;

    @JsonProperty("views_available")
    private List<ViewsEntity> viewsAvailable;

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(id)
                .setAccountNumber(id)
                .setBalance(new Amount(balance.getCurrency(), balance.getAmount()))
                .setAlias(getOwnerName())
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, id))
                .addHolderName(getOwnerName())
                .setApiIdentifier(id)
                .putInTemporaryStorage(OpenBankProjectConstants.StorageKeys.ACCOUNT_ID, id)
                .putInTemporaryStorage(OpenBankProjectConstants.StorageKeys.BANK_ID, bankId)
                .build();
    }

    private String getOwnerName() {
        return owners.stream().findFirst().map(OwnerEntity::getDisplayName).orElse("");
    }

    public String getId() {
        return id;
    }

    public String getBankId() {
        return bankId;
    }

    public BalanceEntity getBalance() {
        return balance;
    }
}
