package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountEntity {

    private String id;
    private String created;
    private String description;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("sort_code")
    private String sortCode;

    private String type;

    @JsonIgnore private BalanceResponse balance;

    public void setBalance(BalanceResponse balance) {
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(
                        MonzoConstants.ACCOUNT_TYPE.translate(type).get(),
                        accountNumber,
                        balance.getTotalBalance())
                .setAccountNumber(accountNumber)
                .setBankIdentifier(this.getId())
                .setName(description)
                .build();
    }
}
