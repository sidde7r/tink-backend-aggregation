package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountsItemEntity {

    private String accountId;

    private String bban;

    private String ownerName;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    private String iban;

    private String accountType;

    private String name;

    private String currency;

    private String bic;

    private String clearingNumber;

    public String getAccountId() {
        return accountId;
    }

    public String getBban() {
        return bban;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public LinksEntity getLinksEntity() {
        return linksEntity;
    }

    public String getIban() {
        return iban;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBic() {
        return bic;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public TransactionalAccount createCheckingAccount(BalancesItemEntity balance) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                                .build())
                .withBalance(BalanceModule.of(getAmount(balance)))
                .addHolderName(ownerName)
                .setApiIdentifier(accountId)
                .setBankIdentifier(getAccountNumber())
                .build();
    }

    private String getAccountNumber() {
        return iban.substring(iban.length() - 9);
    }

    private Amount getAmount(BalancesItemEntity balance) {
        return new Amount(
                balance.getAmountEntity().getCurrency(), balance.getAmountEntity().getContent());
    }
}
