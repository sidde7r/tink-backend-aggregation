package se.tink.backend.rpc;

import io.protostuff.Tag;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import se.tink.backend.core.Account;

public class AccountListResponse {
    @Tag(1)
    @ApiModelProperty(name = "accounts", value="A list of accounts")
    private List<Account> accounts;

    public AccountListResponse() {
        super();
    }

    public AccountListResponse(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}
