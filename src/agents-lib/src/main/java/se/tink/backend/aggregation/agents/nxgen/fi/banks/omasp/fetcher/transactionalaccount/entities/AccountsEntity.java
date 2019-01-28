package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountsEntity {

    private String id;
    private String iban;
    private AmountEntity balance;
    private AmountEntity availableBalance;
    private String name;
    private String ownerName;
    private Boolean detailsAvailable;

    public String getId() {
        return this.id;
    }

    public String getIban() {
        return this.iban;
    }

    public AmountEntity getBalance() {
        return this.balance;
    }

    public AmountEntity getAvailableBalance() {
        return this.availableBalance;
    }

    public String getName() {
        return this.name;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public Boolean getDetailsAvailable() {
        return this.detailsAvailable;
    }

    public TransactionalAccount toTransactionalAccount(AccountTypes accountType) {
        return TransactionalAccount.builder(accountType, this.id, Amount.inEUR(this.availableBalance.getValue()))
                .setAccountNumber(this.id)
                .setBankIdentifier(this.id)
                .setName(this.name)
                .build();
    }
}
