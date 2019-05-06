package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountEntity {

    private String type;
    private String id;
    private String name;
    private String userDefinedName;
    private String owner;
    private String providerId;
    private String kind;
    private Integer automaticRefreshInterval;
    private String number;
    private String bankCodeNumber;
    private String bic;
    private String iban;
    private String currency;
    private BalanceEntity balance;

    @JsonIgnore
    public CheckingAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(balance.getAmount().toTinkAmount())
                .setAlias(name)
                .addAccountIdentifier(new IbanIdentifier(iban))
                .setApiIdentifier(id)
                .build();
    }

    @JsonIgnore
    public boolean isCheckingAccount() {
        return AhoiSandboxConstants.ACCOUNT_TYPE_MAPPER
                .translate(kind)
                .map(AccountTypes.CHECKING::equals)
                .orElse(false);
    }
}
