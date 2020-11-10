package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String accountId;
    private String name;
    private String nickname;
    private BigDecimal balance;
    private String currency;
    private String identifierScheme;
    private String identifier;
    private String servicerScheme;
    private String servicerIdentifier;

    public String getAccountId() {
        return accountId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getName() {
        return name;
    }

    public String getIdentifierScheme() {
        return identifierScheme;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getServicerScheme() {
        return servicerScheme;
    }

    public String getServicerIdentifier() {
        return servicerIdentifier;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        OpBankConstants.ACCOUNT_TYPE_MAPPER, name, TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId)
                                .withAccountNumber(identifier)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(identifier))
                                .build())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, getAccountId())
                .setBankIdentifier(accountId)
                .setApiIdentifier(accountId)
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .build();
    }

    public ExactCurrencyAmount getAvailableBalance() {
        return new ExactCurrencyAmount(getBalance(), getCurrency());
    }
}
