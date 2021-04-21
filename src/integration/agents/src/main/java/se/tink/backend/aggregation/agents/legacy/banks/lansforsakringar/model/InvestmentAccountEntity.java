package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentAccountEntity {
    private String accountNumber;
    private String balance;
    private String customName;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Double getBalance() {
        return balance == null || balance.isEmpty() ? null : StringUtils.parseAmount(balance);
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public Account toAccount(Double totalValue) {
        Account account = new Account();

        account.setAccountNumber(getAccountNumber());
        account.setBankId(getAccountNumber());
        account.setName(getCustomName().isEmpty() ? getAccountNumber() : getCustomName());
        account.setType(AccountTypes.INVESTMENT);
        account.setBalance(totalValue);
        account.setSourceInfo(createAccountSourceInfo());

        return account;
    }

    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder().bankProductName(customName).build();
    }
}
