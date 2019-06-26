package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.util.DemoFinancialInstituteUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private long id;
    private String username;
    private String accountName;
    private double balance;
    private String currency;
    private String accountType;
    private String accountNumber;

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAccountName() {
        return accountName;
    }

    public double getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public boolean isTransactionalAccount() {
        return DemoFinancialInstituteUtils.ACCOUNT_TYPE_MAPPER
                .translate(accountType)
                .orElse(AccountTypes.OTHER)
                .equals(AccountTypes.CHECKING);
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.from(AccountTypes.CHECKING))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountNumber)
                                // TODO: What should the identifier be? Clearing etc?
                                .addIdentifier(new SwedishIdentifier(accountNumber))
                                .build())
                .withBalance(BalanceModule.of(new Amount(currency, balance)))
                .addHolderName(username)
                .setApiIdentifier(accountNumber)
                .setBankIdentifier(accountNumber)
                .build();
    }
}
