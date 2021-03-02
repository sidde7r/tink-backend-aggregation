package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
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
    public boolean isCheckingAccount() {
        return AhoiSandboxConstants.ACCOUNT_TYPE_MAPPER
                .translate(kind)
                .map(AccountTypes.CHECKING::equals)
                .orElse(false);
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(getAccountType())
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(balance.getAmount().toTinkAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(number)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setBankIdentifier(id)
                .build();
    }

    private TransactionalAccountType getAccountType() {
        return AhoiSandboxConstants.ACCOUNT_TYPE_MAPPER.translate(kind).orElse(null);
    }
}
